package com.example.app.models.responses.common;

import com.example.app.models.responses.file.FileResponseEntity;
import com.example.app.models.responses.user.UserResponseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserWithFiles implements FileResponseEntity {
    private UserResponseEntity user;
    private List<FileResponseEntity> files;
}
