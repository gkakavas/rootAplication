package com.example.app.utils.file;

import com.example.app.entities.File;
import com.example.app.entities.FileKind;
import com.example.app.entities.User;
import com.example.app.exception.FileNotFoundException;
import com.example.app.models.responses.file.AdminHrManagerFileResponse;
import com.example.app.models.responses.file.FileResourceResponse;
import com.example.app.models.responses.file.FileResponseEntity;
import com.example.app.models.responses.file.UserFileResponse;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.FileSizeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EntityResponseFileConverterImp implements EntityResponseFileConverter {
    private final UserRepository userRepo;


    @Override
    public FileResponseEntity fromFileToAdmin(File file) {
        var response = AdminHrManagerFileResponse.builder()
                .fileId(file.getFileId())
                .filename(file.getFilename())
                .fileSize(FileSizeConverter.convert(file.getFileSize()))
                .fileType(file.getFileType())
                .uploadDate(file.getUploadDate())
                .approved(file.getApproved())
                .approvedDate(file.getApprovedDate())
                .fileKind(file.getFileKind())
                .build();
        if(file.getApprovedBy()!=null) {
            userRepo.findById(file.getApprovedBy()).ifPresent(
                    value -> response.setApprovedBy(value.getEmail()));
        }
        if(file.getUploadedBy()!=null){
            response.setUploadedBy(file.getUploadedBy().getEmail());
        }
        return response;
    }



    @Override
    public FileResponseEntity fromFileToUser(File file) {
        var response = UserFileResponse.builder()
                .fileId(file.getFileId())
                .filename(file.getFilename())
                .fileSize(FileSizeConverter.convert(file.getFileSize()))
                .approved(file.getApproved())
                .approvedBy(null)
                .approvedDate(file.getApprovedDate())
                .fileKind(file.getFileKind())
                .build();
        if(file.getApprovedBy()!=null) {
            userRepo.findById(file.getApprovedBy()).ifPresent(
                    value -> response.setApprovedBy(value.getEmail()));
        }
        return response;
    }

    @Override
    public FileResponseEntity fromFileToResource(File file) {
        try {
            Path pathToFile = Paths.get(file.getAccessUrl());
            FileSystemResource resource = new FileSystemResource(pathToFile);
            if ((resource.exists() || resource.isReadable())) {
                return FileResourceResponse.builder()
                        .resource(resource)
                        .fileType(file.getFileType())
                        .fileName(file.getFilename())
                        .build();
            }
            else {
                throw new RuntimeException("Could not read the file!");
            }
        }catch (InvalidPathException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public List<FileResponseEntity> fromFileListToAdminList(Set<File> fileList) {
        return fileList.stream().map(this::fromFileToAdmin).toList();
    }

    @Override
    public List<FileResponseEntity> fromFileListToUserFileList(Set<File> fileList) {
        return fileList.stream().map(this::fromFileToUser).toList();
    }

    public File extractMultipartInfo(MultipartFile multipartFile, User fileCreator, String accessUrl, FileKind fileKind){
        return File.builder()
                .filename(multipartFile.getOriginalFilename())
                .fileSize(multipartFile.getSize())
                .fileType(multipartFile.getContentType())
                .uploadDate(LocalDateTime.now())
                .accessUrl(accessUrl)
                .fileKind(fileKind)
                .uploadedBy(fileCreator)
                .build();
    }

    @Override
    public File approveFile(File file, User user){
        file.setApproved(true);
        file.setApprovedBy(user.getUserId());
        file.setApprovedDate(LocalDateTime.now());
        return file;
    }
}


