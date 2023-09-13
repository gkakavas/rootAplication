package com.example.app.services;

import com.example.app.entities.File;
import com.example.app.entities.FileKind;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.FileNotFoundException;
import com.example.app.exception.IllegalTypeOfFileException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.responses.common.UserWithFiles;
import com.example.app.models.responses.file.AdminHrManagerFileResponse;
import com.example.app.models.responses.file.FileResponseEntity;
import com.example.app.models.responses.file.UserFileResponse;
import com.example.app.repositories.FileRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.common.EntityResponseCommonConverter;
import com.example.app.utils.file.EntityResponseFileConverter;
import com.example.app.utils.file.EntityResponseFileConverterImp;
import com.example.app.utils.file.FileContent;
import com.example.app.utils.user.EntityResponseUserConverter;
import com.example.app.utils.user.EntityResponseUserConverterImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final FileRepository fileRepo;
    private final EntityResponseCommonConverter commonConverter;
    private final EntityResponseUserConverter userConverter;
    private final EntityResponseFileConverter fileConverter;
    @Getter
    @Setter
    private Path root = Paths.get("uploads");
    @Getter
    @Setter
    private Path timesheet = root.resolve("timesheets");
    @Getter
    @Setter
    private Path evaluation = root.resolve("evaluations");
    private static FileKind fileKind;

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
            var currentUser = userRepo.findByEmail(SecurityContextHolder.getContext().getAuthentication()
                    .getName()).orElseThrow(UserNotFoundException::new);
            Path userPath = null;

            try {
                if(Objects.equals(file.getContentType(),FileContent.docx.getFileContent())
                        ||Objects.equals(file.getContentType(), FileContent.txt.getFileContent())
                        ||Objects.equals(file.getContentType(),FileContent.rtf.getFileContent())){
                    userPath = Files.createDirectories(evaluation.resolve(currentUser.getUserId().toString()));
                    fileKind = FileKind.EVALUATION;
                }
                else if(Objects.equals(file.getContentType(), FileContent.xls.getFileContent())
                        ||Objects.equals(file.getContentType(), FileContent.xlsx.getFileContent())){
                    userPath = Files.createDirectories(timesheet.resolve(currentUser.getUserId().toString()));
                    fileKind = FileKind.TIMESHEET;
                }
                else{
                    throw new IllegalTypeOfFileException();
                }
                Files.copy(file.getInputStream(),userPath.resolve(Objects.requireNonNull(file.getOriginalFilename())));
                return saveInDatabase(file,currentUser,userPath,fileKind);
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


    public FileResponseEntity download(UUID fileId,FileKind fileKind) throws UserNotFoundException, FileNotFoundException {
        if (fileRepo.existsByFileIdAndFileKind(fileId, fileKind)) {
                var file = fileRepo.findById(fileId).orElseThrow(FileNotFoundException::new);
                var user = userRepo.findByEmail(SecurityContextHolder.getContext()
                        .getAuthentication().getName()).orElseThrow(UserNotFoundException::new);
                var response = fileConverter.fromFileToResource(file);
                if(Arrays.stream(Role.values()).toList().contains(user.getRole())){
                    return response;
                }
                else throw new AccessDeniedException("You have not authority to access this resource");

        }
        else throw new FileNotFoundException();
    }

    public Set<UserWithFiles> readAll(FileKind fileKind) throws UserNotFoundException {
        var user = userRepo.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(UserNotFoundException::new);
        if(user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.HR) && fileKind.equals(FileKind.TIMESHEET))){
            var files = fileRepo.findAllByFileKind(fileKind);
            Set<User> users = new HashSet<>();
            for(File file:files){
                users.add(file.getUploadedBy());
            }
            return commonConverter.usersWithFilesList(users);
        }
        else if(user.getRole().equals(Role.MANAGER) && fileKind.equals(FileKind.EVALUATION)){
            var files = fileRepo.findAllByFileKindAndUploadedBy_Group(FileKind.EVALUATION,user.getGroup());
            Set<User> users = new HashSet<>();
            for(File file:files){
                users.add(file.getUploadedBy());
            }
            return commonConverter.usersWithFilesList(users);
        }
        else if(user.getRole().equals(Role.USER)){
            Set<File> files =
                    new HashSet<>(fileRepo.findAllByFileKindAndUploadedBy(fileKind, user));
            Set<UserWithFiles> userFilesList = new HashSet<>();
            userFilesList.add(new UserWithFiles(
                    userConverter.fromUserToOtherUser(user),
                    fileConverter.fromFileListToUserFileList(files)));
            return userFilesList;
        }
       else throw new AccessDeniedException("You have not authority to access this resource");
    }

    public boolean delete(UUID fileId) throws FileNotFoundException, UserNotFoundException {
        var file = fileRepo.findById(fileId).orElseThrow(FileNotFoundException::new);
        var currentUser = userRepo.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(UserNotFoundException::new);
        if (file.getUploadedBy().getEmail().equals(currentUser.getEmail())) {
            Path pathOfFile = Path.of(file.getAccessUrl());
            try {
                Files.delete(pathOfFile);
                fileRepo.deleteById(fileId);
                return true;
            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
                return false;
            }
        }
        else throw new AccessDeniedException("You have not authority to execute this operation");
    }

    public void deleteAll() {
        fileRepo.deleteAll();
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    public FileResponseEntity approveEvaluation(UUID fileId) throws UserNotFoundException, FileNotFoundException {
        var currentUser = userRepo.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(UserNotFoundException::new);
        var file = fileRepo.findById(fileId).orElseThrow(FileNotFoundException::new);
        if(currentUser.getGroup().equals(file.getUploadedBy().getGroup())&&file.getFileKind().equals(FileKind.EVALUATION)){
            var approveFile = fileConverter.approveFile(file,currentUser);
            var approvedFile = fileRepo.save(approveFile);
            return fileConverter.fromFileToAdmin(approvedFile);
        }
        else throw new AccessDeniedException("You have not authority to approve this evaluation");
    }

    public FileResponseEntity saveInDatabase(MultipartFile file, User user, Path userPath, FileKind fileKind){
        var newFile = fileConverter.extractMultipartInfo(file,user,
                userPath.resolve(Objects.requireNonNull(file.getOriginalFilename())).toString(),fileKind);
        var response = fileRepo.save(newFile);
        return fileConverter.fromFileToUser(response);
    }
}
