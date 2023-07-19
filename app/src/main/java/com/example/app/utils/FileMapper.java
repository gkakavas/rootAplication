package com.example.app.utils;

import com.example.app.entities.File;
import com.example.app.entities.User;
import com.example.app.models.responses.FileStorageProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class FileMapper {
    public File extractMultipartInfo(MultipartFile multipartFile,User fileCreator,String accessUrl){
        return File.builder()
        .filename(multipartFile.getOriginalFilename())
        .fileSize(multipartFile.getSize())
        .fileType(multipartFile.getContentType())
        .uploadDate(LocalDateTime.now())
        .accessUrl(accessUrl)
        .uploadedBy(fileCreator)
        .build();
    }

    public FileStorageProperties convertToResponse(File file){
        return FileStorageProperties.builder()
                .fileId(file.getFileId())
                .filename(file.getFilename())
                .fileSize(file.getFileSize())
                .fileType(file.getFileType())
                .uploadDate(file.getUploadDate())
                .accessUrl(file.getAccessUrl())
                .approved(file.getApproved())
                .approvedBy(file.getApprovedBy())
                .approvedDate(file.getApprovedDate())
                .uploadedBy(file.getUploadedBy())
                .build();
    }
}
