package com.example.app.models.responses;

import com.example.app.entities.User;
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
    private String accessUrl;
    private Boolean approved;
    private UUID approvedBy;
    private LocalDateTime approvedDate;
    private User uploadedBy;
}
