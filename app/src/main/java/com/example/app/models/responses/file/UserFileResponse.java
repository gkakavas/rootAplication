package com.example.app.models.responses.file;

import com.example.app.entities.FileKind;
import com.example.app.models.responses.user.OtherUserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Builder
@AllArgsConstructor
public class UserFileResponse implements FileResponseEntity{
    private UUID fileId;
    private String filename;
    private Long fileSize;
    private Boolean approved;
    private String approvedBy;
    private LocalDateTime approvedDate;
    private FileKind fileKind;
}
