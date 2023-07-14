package com.example.app.utils;

import com.example.app.entities.File;
import com.example.app.entities.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class FileMapper {
    public File extractMultipartInfo(MultipartFile multipartFile,UUID fileCreator,String accessUrl){
        File file = new File();
        file.setFilename(multipartFile.getOriginalFilename());
        file.setFileSize(multipartFile.getSize());
        file.setFileType(multipartFile.getContentType());
        file.setUploadDate(LocalDateTime.now());
        file.setAccessUrl(accessUrl);
        file.setApproved(file.getApproved());
        file.setApprovedBy(file.getApprovedBy());
        file.setApprovedDate(file.getApprovedDate());
        //file.setUploadedBy(fileCreator);
        return file;
    }
}
