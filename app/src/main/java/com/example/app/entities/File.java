package com.example.app.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="_file")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @NotNull
    private UUID fileId;
    private String filename;
    private Float fileSize;
    private String fileType;
    private LocalDateTime uploadDate;
    private UUID uploadedBy;
    private String pathToServer;
    private String accessUrl;
    private Boolean approved;
    private UUID approvedBy;
    private LocalDateTime approvedDate;
}
