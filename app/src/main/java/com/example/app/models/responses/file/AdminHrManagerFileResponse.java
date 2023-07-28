package com.example.app.models.responses.file;

import com.example.app.entities.FileKind;
import com.example.app.entities.User;
import com.example.app.models.responses.user.OtherUserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class AdminHrManagerFileResponse implements FileResponseEntity {
    private UUID fileId;
    private String filename;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadDate;
    @Nullable
    private Boolean approved;
    @Nullable
    private String approvedBy;
    @Nullable
    private LocalDateTime approvedDate;
    private FileKind fileKind;

}
