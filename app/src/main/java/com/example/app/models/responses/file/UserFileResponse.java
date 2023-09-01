package com.example.app.models.responses.file;

import com.example.app.entities.FileKind;
import com.example.app.models.responses.user.OtherUserResponse;
import com.example.app.utils.file.UrlResourceSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Builder
@AllArgsConstructor
public class UserFileResponse implements FileResponseEntity, Serializable {
    private UUID fileId;
    private String filename;
    private String fileSize;
    private Boolean approved;
    private String approvedBy;
    private LocalDateTime approvedDate;
    private FileKind fileKind;
}
