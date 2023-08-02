package com.example.app.services;

import com.example.app.entities.File;
import com.example.app.entities.FileKind;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.FileNotFoundException;
import com.example.app.exception.IllegalTypeOfFileException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.responses.common.UserWithFiles;
import com.example.app.models.responses.file.FileResponseEntity;
import com.example.app.repositories.FileRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.common.EntityResponseCommonConverter;
import com.example.app.utils.file.EntityResponseFileConverter;
import com.example.app.utils.user.EntityResponseUserConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final FileRepository fileRepo;
    private final FileDownloadAuthorityService fileDownloadAuthService;
    private final EntityResponseCommonConverter commonConverter;
    private final EntityResponseUserConverter userConverter;
    private final EntityResponseFileConverter fileConverter;
    private final Path root = Paths.get("uploads");
    private final Path timesheet = root.resolve("timesheets");
    private final Path evaluation = root.resolve("evaluations");
    private FileKind fileKind;
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

    public FileResponseEntity upload(MultipartFile file, String token)
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
                return fileConverter.fromFileToUser(fileRepo.save(oldFile));
            }
            catch (IOException e ) {
                    throw new RuntimeException("Error encountered while store the file");
            }
    }

    public Resource download(UUID fileId,FileKind fileKind) throws UserNotFoundException, FileNotFoundException {
        if (fileRepo.existsByFileIdAndFileKind(fileId, fileKind)) {
            try {
                var path = fileDownloadAuthService.checkAuthority(fileId);
                Resource resource = new UrlResource(path.toUri());
                if (resource.exists() || resource.isReadable()) {
                    return resource;
                } else {
                    throw new RuntimeException("Could not read the file!");
                }
            } catch (MalformedURLException | FileNotFoundException e) {
                throw new RuntimeException("Error: " + e.getMessage());
            }
        }
        else throw new FileNotFoundException();
    }

    public Set<UserWithFiles> readAll(FileKind fileKind) throws UserNotFoundException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow(UserNotFoundException::new);
        if(user.getRole().equals(Role.ROLE_ADMIN)||user.getRole().equals(Role.ROLE_HR)){
            var files = fileRepo.findAllByFileKind(fileKind);
            Set<User> users = new HashSet<>();
            for(File file:files){
                users.add(file.getUploadedBy());
            }
            return commonConverter.usersWithFilesList(users);
        }
        else if(user.getRole().equals(Role.ROLE_MANAGER)){
            var files = fileRepo.findAllByFileKindAndUploadedBy_Group(FileKind.EVALUATION,user.getGroup());
            Set<User> users = new HashSet<>();
            for(File file:files){
                users.add(file.getUploadedBy());
            }
            return commonConverter.usersWithFilesList(users);
        }
        else if(user.getRole().equals(Role.ROLE_USER)){
            Set<File> files =
                    new HashSet<>(fileRepo.findAllByFileKindAndUploadedBy_Group(fileKind, user.getGroup()));
            Set<UserWithFiles> userFilesList = new HashSet<>();
            userFilesList.add(new UserWithFiles(
                    userConverter.fromUserToOtherUser(user),
                    fileConverter.fromFileListToUserFileList(files)));
            return userFilesList;
        }
       else throw new AccessDeniedException("You have not authority to access this resource");
    }

    public boolean delete(UUID fileId) throws FileNotFoundException {
        var file = fileRepo.findById(fileId).orElseThrow(FileNotFoundException::new);
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
        fileRepo.deleteAll();
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    public FileResponseEntity saveInDatabase(MultipartFile file, User user, Path userPath, FileKind fileKind){
        var newFile = fileConverter.extractMultipartInfo(file,user,
                userPath.resolve(Objects.requireNonNull(file.getOriginalFilename())).toString(),fileKind);
        return fileConverter.fromFileToUser(fileRepo.save(newFile));
    }
}
