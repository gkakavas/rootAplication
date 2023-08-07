package com.example.app.utils.file;

import com.example.app.entities.File;
import com.example.app.entities.FileKind;
import com.example.app.entities.User;
import com.example.app.models.responses.file.AdminHrManagerFileResponse;
import com.example.app.models.responses.file.FileResponseEntity;
import com.example.app.models.responses.file.UserFileResponse;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.FileSizeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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
                .fileSize(FileSizeConverter.converter(file.getFileSize()))
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
                .fileSize(FileSizeConverter.converter(file.getFileSize()))
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
    public List<FileResponseEntity> fromFileListToAdminList(Set<File> fileList) {
        List<FileResponseEntity> responseList = new ArrayList<>();
        fileList.forEach((file)->responseList.add(fromFileToAdmin(file)));
        return responseList;
    }

    @Override
    public List<FileResponseEntity> fromFileListToUserFileList(Set<File> fileList) {
        List<FileResponseEntity> responseList = new ArrayList<>();
        fileList.forEach((file)->responseList.add(fromFileToUser(file)));
        return responseList;
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


