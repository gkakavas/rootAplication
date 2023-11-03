package com.example.app.services;

import com.example.app.config.FileStorageProperties;
import com.example.app.entities.FileKind;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.FileNotFoundException;
import com.example.app.exception.IllegalTypeOfFileException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.responses.file.FileResponseEntity;
import com.example.app.repositories.FileRepository;
import com.example.app.utils.file.EntityResponseFileConverter;
import com.example.app.utils.file.FileContent;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final FileRepository fileRepo;
    private final EntityResponseFileConverter fileConverter;
    private final FileStorageProperties storageProperties;

    public FileResponseEntity upload(MultipartFile file,Principal connectedUser)
            throws IllegalTypeOfFileException, IOException {
            var currentUser = (User)((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
            Path userPath = null;
            try {
                FileKind fileKind;
                if(List.of(FileContent.docx.name(),FileContent.txt.name(),FileContent.rtf.name())
                        .contains(file.getContentType())){
                    userPath = storageProperties.getEvaluation().resolve(currentUser.getUserId().toString());
                    Files.createDirectory(userPath);
                    fileKind = FileKind.EVALUATION;
                }
                else if(List.of(FileContent.xlsx.name(),FileContent.xls.name()).contains(file.getContentType())){
                    userPath = storageProperties.getTimesheet().resolve(currentUser.getUserId().toString());
                    Files.createDirectory(userPath);
                    fileKind = FileKind.TIMESHEET;
                }
                else{
                    throw new IllegalTypeOfFileException();
                }
                Files.copy(file.getInputStream(),userPath.resolve(Objects.requireNonNull(file.getOriginalFilename())));
                return saveInDatabase(file,currentUser,userPath, fileKind);
            }
            catch(FileAlreadyExistsException e){
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


    public FileResponseEntity download(UUID fileId,FileKind fileKind,Principal connectedUser) throws UserNotFoundException, FileNotFoundException {
        if (fileRepo.existsByFileIdAndFileKind(fileId, fileKind)) {
            var file = fileRepo.findById(fileId).orElseThrow(FileNotFoundException::new);
                var currentUser = (User)((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();;
                if(currentUser.getRole().equals(Role.ADMIN)){
                    return fileConverter.fromFileToResource(file);
                }
                else if(file.getFileKind().equals(FileKind.TIMESHEET) && currentUser.getRole().equals(Role.HR)){
                    return fileConverter.fromFileToResource(file);
                }
                else if (file.getFileKind().equals(FileKind.EVALUATION)
                        && currentUser.getRole().equals(Role.MANAGER)
                        && file.getUploadedBy().getGroup().equals(currentUser.getGroup())){
                    return fileConverter.fromFileToResource(file);
                }
                else if(currentUser.getRole().equals(Role.USER) && currentUser.getUserHasFiles().contains(file)){
                    return fileConverter.fromFileToResource(file);
                }
                else throw new AccessDeniedException("You have not authority to download this resource");
        }
        else throw new FileNotFoundException();
    }

    public Set<FileResponseEntity> readAll(FileKind fileKind,Principal connectedUser) throws UserNotFoundException {
        var currentUser = (User)((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if(currentUser.getRole().equals(Role.ADMIN)){
            var files = fileRepo.findAllByFileKind(fileKind);
            return Set.copyOf(fileConverter.fromFileListToAdminList(Set.copyOf(files)));
        }
        else if(currentUser.getRole().equals(Role.HR) && fileKind.equals(FileKind.TIMESHEET)){
            var files = fileRepo.findAllByFileKind(FileKind.TIMESHEET);
            return Set.copyOf(fileConverter.fromFileListToAdminList(Set.copyOf(files)));
        }
        else if(currentUser.getRole().equals(Role.MANAGER) && fileKind.equals(FileKind.EVALUATION)){
            var files = fileRepo.findAllByFileKindAndUploadedBy_Group(FileKind.EVALUATION,currentUser.getGroup());
            return Set.copyOf(fileConverter.fromFileListToAdminList(Set.copyOf(files)));
        }
        else if(currentUser.getRole().equals(Role.USER)){
            var files = new HashSet<>(fileRepo.findAllByFileKindAndUploadedBy(fileKind, currentUser));
            return Set.copyOf(fileConverter.fromFileListToUserFileList(files));
        }
       else throw new AccessDeniedException("You have not authority to access this resource");
    }

    public boolean delete(UUID fileId) throws FileNotFoundException, UserNotFoundException {
        var file = fileRepo.findById(fileId).orElseThrow(FileNotFoundException::new);
            Path pathOfFile = Path.of(file.getAccessUrl());
            try {
                Files.delete(pathOfFile);
                file.getUploadedBy().getUserHasFiles().remove(file);
                fileRepo.deleteById(fileId);
                return true;
            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
                return false;
            }
    }

    public FileResponseEntity approveEvaluation(UUID fileId,Principal connectedUser) throws FileNotFoundException {
        var currentUser = (User)((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        var file = fileRepo.findById(fileId).orElseThrow(FileNotFoundException::new);
        if(currentUser.getRole().equals(Role.ADMIN)
                || (currentUser.getGroup().equals(file.getUploadedBy().getGroup())
                && file.getFileKind().equals(FileKind.EVALUATION))){
            var approvedFile = fileConverter.approveFile(file,currentUser);
            var patchedFile = fileRepo.save(approvedFile);
            return fileConverter.fromFileToAdmin(patchedFile);
        }
        else throw new AccessDeniedException("You have not authority to approve this evaluation");
    }

    public FileResponseEntity saveInDatabase(MultipartFile file, User user, Path userPath, FileKind fileKind){
        var newFile = fileConverter.extractMultipartInfo(file,user,
                userPath.resolve(Objects.requireNonNull(file.getOriginalFilename())).toString(),fileKind);
        var response = fileRepo.save(newFile);
        return fileConverter.fromFileToUser(response);
    }

    public void deleteAll() {
        fileRepo.deleteAll();
        FileSystemUtils.deleteRecursively(storageProperties.getRoot().toFile());
    }

}
