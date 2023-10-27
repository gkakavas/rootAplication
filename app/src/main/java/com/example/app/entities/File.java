package com.example.app.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString(exclude = "uploadedBy")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "File")
@Table(name="files")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @NotNull
    private UUID fileId;
    private String filename;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadDate;
    private String accessUrl;
    private Boolean approved;
    private UUID approvedBy;
    private LocalDateTime approvedDate;
    @Enumerated(value = EnumType.STRING)
    private FileKind fileKind;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User uploadedBy;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        File file = (File) o;

        return new EqualsBuilder().append(fileId, file.fileId).append(filename, file.filename).append(fileSize, file.fileSize).append(fileType, file.fileType).append(uploadDate, file.uploadDate).append(accessUrl, file.accessUrl).append(approved, file.approved).append(approvedBy, file.approvedBy).append(approvedDate, file.approvedDate).append(fileKind, file.fileKind).append(uploadedBy, file.uploadedBy).isEquals();
    }
}
