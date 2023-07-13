package com.example.app.models.requests;

import com.example.app.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileRequestEntity {
    private String filename;
    private Float fileSize;
    private String fileType;
    private String accessUrl;
    private Boolean approved;
    private LocalDateTime approvedDate;
    private User uploadedBy;
}
