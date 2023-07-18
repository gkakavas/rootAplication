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
        File file = new File();
        file.setFilename(multipartFile.getOriginalFilename());
        file.setFileSize(multipartFile.getSize());
        file.setFileType(multipartFile.getContentType());
        file.setUploadDate(LocalDateTime.now());
        file.setAccessUrl(accessUrl);
        file.setUploadedBy(fileCreator);
        return file;
    }

    public FileStorageProperties convertToResponse(File file){
        FileStorageProperties response = new FileStorageProperties();
        response.setFileId(file.getFileId());
        response.setFilename(file.getFilename());
        response.setFileSize(file.getFileSize());
        response.setFileType(file.getFileType());
        response.setUploadDate(file.getUploadDate());
        response.setAccessUrl(file.getAccessUrl());
        response.setApproved(file.getApproved());
        response.setApprovedBy(file.getApprovedBy());
        response.setApprovedDate(file.getApprovedDate());
        response.setUploadedBy(file.getUploadedBy());

        return response;
    }
}
