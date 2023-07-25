package com.example.app.services;

import com.example.app.entities.File;
import com.example.app.entities.User;
import com.example.app.exception.IllegalTypeOfFileException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.responses.FileStorageProperties;
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
    private final Path root = Paths.get("uploads");
    private final Path timesheet = root.resolve("timesheets");
    private final Path evaluation = root.resolve("evaluations");
    public static final String
            txt = "text/plain",
            docx = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            xls = "application/vnd.ms-excel",
            xlsx = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public void init() {
        try {
            Files.createDirectories(timesheet);
            Files.createDirectories(evaluation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    public FileStorageProperties upload(MultipartFile file, String token)
    throws UserNotFoundException,IllegalTypeOfFileException{
            var user = userRepo.findByEmail(jwtService.extractUsername
                    (token.substring(7))).orElseThrow(UserNotFoundException::new);
            try {
                if(Objects.equals(file.getContentType(), docx) || Objects.equals(file.getContentType(), txt)){
                    var userPath = Files.createDirectories(evaluation.resolve(user.getUserId().toString()));
                    Files.copy(file.getInputStream(),userPath.resolve(Objects.requireNonNull(file.getOriginalFilename())));
                    return saveInDatabase(file,user,userPath);
                }
                else if(Objects.equals(file.getContentType(), xls) || Objects.equals(file.getContentType(), xlsx)){
                    var userPath = Files.createDirectories(timesheet.resolve(user.getUserId().toString()));
                    Files.copy(file.getInputStream(),userPath.resolve(Objects.requireNonNull(file.getOriginalFilename())));
                    return saveInDatabase(file,user,userPath);
                }
                else{
                    throw new IllegalTypeOfFileException();
                }
            } catch (IOException e) {
                    throw new RuntimeException("A file of that name already exists.");
            }
    }

    public Resource download(UUID fileId) {
        var file = fileRepo.findById(fileId).orElseThrow(()->new RuntimeException("Not Found file with this id"));
            try {
                Path pathToFile = Path.of(file.getAccessUrl());
                Resource resource = new UrlResource(pathToFile.toUri());
                if (resource.exists() || resource.isReadable()) {
                    return resource;
                }
                else {
                    throw new RuntimeException("Could not read the file!");
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException("Error: " + e.getMessage());
            }
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
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

    public FileStorageProperties saveInDatabase(MultipartFile file, User user,Path userPath){
        var newFile = fileMapper.extractMultipartInfo(file,user,
                userPath.resolve(Objects.requireNonNull(file.getOriginalFilename())).toString());
        return fileMapper.convertToResponse(fileRepo.save(newFile));
    }
}
