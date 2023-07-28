package com.example.app.utils.file;

import com.example.app.entities.File;
import com.example.app.entities.User;
import com.example.app.models.responses.file.AdminHrManagerFileResponse;
import com.example.app.models.responses.file.UserFileResponse;
import com.example.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class EntityResponseFileConverterImp implements EntityResponseFileConverter{
    private final UserRepository userRepo;
    @Override
    public AdminHrManagerFileResponse fromFileToAdmin(File file) {
        var response =  AdminHrManagerFileResponse.builder()
                .fileId(file.getFileId())
                .filename(file.getFilename())
                .fileSize(file.getFileSize())
                .fileType(file.getFileType())
                .uploadDate(file.getUploadDate())
                .approved(file.getApproved())
                .approvedBy(null)
                .approvedDate(file.getApprovedDate())
                .fileKind(file.getFileKind())
                .build();
        try{
            response.setApprovedBy(userRepo.findById(file.getApprovedBy()).orElseThrow().getEmail());
        }
        catch (NoSuchElementException e){
            response.setApprovedBy(null);
        }
        return response;
    }

    @Override
    public UserFileResponse fromFileToUser(File file) {
        var response = UserFileResponse.builder()
                .fileId(file.getFileId())
                .filename(file.getFilename())
                .fileSize(file.getFileSize())
                .approved(file.getApproved())
                .approvedBy(null)
                .approvedDate(file.getApprovedDate())
                .fileKind(file.getFileKind())
                .build();
        try{
            response.setApprovedBy(userRepo.findById(file.getApprovedBy()).orElseThrow().getEmail());
        }
        catch (NoSuchElementException e){
            response.setApprovedBy(null);
        }
        return response;
    }

    @Override
    public List<AdminHrManagerFileResponse> fromFileListToAdminList(List<User> userList) {
        List<AdminHrManagerFileResponse> responseList = new ArrayList<>();
        for(User user:userList){
            responseList.add(user.getUserHasFiles())
        }
    }


    @Override
    public List<UserFileResponse> fromFileListToUserList(List<User> userList) {
        return null;
    }
}
