package com.example.app.models.responses.file;

import com.example.app.entities.FileKind;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminHrManagerFileResponse implements FileResponseEntity, Serializable {
    private UUID fileId;
    private String filename;
    private String fileSize;
    private String fileType;
    private LocalDateTime uploadDate;
    private Boolean approved;
    private String approvedBy;
    private LocalDateTime approvedDate;
    private FileKind fileKind;
    private String uploadedBy;
}
