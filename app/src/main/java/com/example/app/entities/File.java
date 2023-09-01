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
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadDate;
    private String accessUrl;
    private Boolean approved;
    private UUID approvedBy;
    private LocalDateTime approvedDate;
    @Enumerated(value = EnumType.STRING)
    private FileKind fileKind;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id")
    private User uploadedBy;
}
