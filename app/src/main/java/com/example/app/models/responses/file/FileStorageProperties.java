package com.example.app.models.responses.file;

import com.example.app.entities.FileKind;
import com.example.app.entities.User;
import com.example.app.models.responses.user.AdminUserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileStorageProperties {
    private UUID fileId;
    private String filename;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadDate;
    private Boolean approved;
    private UUID approvedBy;
    private LocalDateTime approvedDate;
    private UUID uploadedBy;
    private FileKind fileKind;
}
