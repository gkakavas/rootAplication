package com.example.app.utils.file;

import com.example.app.entities.File;
import com.example.app.entities.FileKind;
import com.example.app.entities.User;
import com.example.app.models.responses.file.AdminHrManagerFileResponse;
import com.example.app.models.responses.file.FileResponseEntity;
import com.example.app.models.responses.file.UserFileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface EntityResponseFileConverter {
     FileResponseEntity fromFileToAdmin(File file);
     FileResponseEntity fromFileToUser(File file);
     List<FileResponseEntity> fromFileListToAdminList(Set<File> fileList);
     List<FileResponseEntity> fromFileListToUserFileList(Set<File> fileList);
     File extractMultipartInfo(MultipartFile multipartFile, User fileCreator, String accessUrl, FileKind fileKind);

     File approveFile(File file, User user);
}
