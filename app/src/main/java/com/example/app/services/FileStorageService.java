package com.example.app.services;

import com.example.app.entities.File;
import com.example.app.entities.FileKind;
import com.example.app.entities.User;
import com.example.app.exception.FileNotFoundException;
import com.example.app.exception.IllegalTypeOfFileException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.responses.file.FileStorageProperties;
import com.example.app.repositories.FileRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.FileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final FileRepository fileRepo;
    private final FileMapper fileMapper;
    private final TimesheetFileAuthorityCheckService timesheetAuthCheckService;
    private final Path root = Paths.get("uploads");
    private final Path timesheet = root.resolve("timesheets");
    private final Path evaluation = root.resolve("evaluations");
    public static final String
            txt = "text/plain",
            docx = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            xls = "application/vnd.ms-excel",
            xlsx = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private FileKind fileKind;

    public void init() {
        try {
            Files.createDirectories(timesheet);
            Files.createDirectories(evaluation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    public FileStorageProperties upload(MultipartFile file, String token)
            throws UserNotFoundException, IllegalTypeOfFileException, IOException {
            var user = userRepo.findByEmail(jwtService.extractUsername
                    (token.substring(7))).orElseThrow(UserNotFoundException::new);
            Path userPath = null;

            try {
                if(Objects.equals(file.getContentType(), docx) || Objects.equals(file.getContentType(), txt)){
                    userPath = Files.createDirectories(evaluation.resolve(user.getUserId().toString()));
                    fileKind = FileKind.EVALUATION;
                }
                else if(Objects.equals(file.getContentType(), xls) || Objects.equals(file.getContentType(), xlsx)){
                    userPath = Files.createDirectories(timesheet.resolve(user.getUserId().toString()));
                    fileKind = FileKind.TIMESHEET;
                }
                else{
                    throw new IllegalTypeOfFileException();
                }
                Files.copy(file.getInputStream(),userPath.resolve(Objects.requireNonNull(file.getOriginalFilename())));
                return saveInDatabase(file,user,userPath,fileKind);
            }
            catch(FileAlreadyExistsException e){
                assert userPath != null;
                Path destinationFilePath = userPath.resolve(Objects.requireNonNull(file.getOriginalFilename()));
                Files.copy(file.getInputStream(), destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
                var oldFile = fileRepo.findFileByAccessUrl(destinationFilePath.toString());
                oldFile.setFileSize(file.getSize());
                oldFile.setFileType(file.getContentType());
                oldFile.setUploadDate(LocalDateTime.now());
                return fileMapper.convertToResponse(fileRepo.save(oldFile));
            }
            catch (IOException e ) {
                    throw new RuntimeException("Error encountered while store the file");
            }
    }

    public Resource download(UUID fileId) {
            try {
                var path = timesheetAuthCheckService.checkAuthority(fileId);
                Resource resource = new UrlResource(path.toUri());
                if (resource.exists() || resource.isReadable()) {
                    return resource;
                }
                else {
                    throw new RuntimeException("Could not read the file!");
                }
            } catch (MalformedURLException | FileNotFoundException e) {
                throw new RuntimeException("Error: " + e.getMessage());
            }
    }


    public List<File> readAll(UUID userId) {
        var files = fileRepo.findAllByUploadedBy(userRepo.findById(userId).orElse(null));
        if(!files.isEmpty()){
            return files;
        }
        else
            throw new RuntimeException("This user has not files");
    }

    public boolean delete(UUID fileId) {
        var file = fileRepo.findById(fileId).orElseThrow(()
                ->new IllegalArgumentException("Not found file with this id"));
        Path pathOfFile = Path.of(file.getAccessUrl());
        try {
            Files.delete(pathOfFile);
            fileRepo.deleteById(fileId);
            return true;
        }catch(IOException|IllegalArgumentException e){
            e.printStackTrace();
            return false;
        }
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    public FileStorageProperties saveInDatabase(MultipartFile file, User user,Path userPath,FileKind fileKind){
        var newFile = fileMapper.extractMultipartInfo(file,user,
                userPath.resolve(Objects.requireNonNull(file.getOriginalFilename())).toString(),fileKind);
        return fileMapper.convertToResponse(fileRepo.save(newFile));
    }
}
