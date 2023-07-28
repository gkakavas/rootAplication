package com.example.app.utils.file;

import com.example.app.entities.File;
import com.example.app.entities.User;
import com.example.app.models.responses.file.AdminHrManagerFileResponse;
import com.example.app.models.responses.file.UserFileResponse;

import java.util.List;

public interface EntityResponseFileConverter {
     AdminHrManagerFileResponse fromFileToAdmin(File file);
     UserFileResponse fromFileToUser(File file);
     List<AdminHrManagerFileResponse> fromFileListToAdminList(List<User> userList);
     List<UserFileResponse> fromFileListToUserList(List<User> userList);
}
