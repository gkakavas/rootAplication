package com.example.app.repositories;

import com.example.app.entities.File;
import com.example.app.entities.FileKind;
import com.example.app.entities.Group;
import com.example.app.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface FileRepository extends JpaRepository<File, UUID> {
    List<File> findAllByUploadedBy(User uploadedBy);
    File findFileByAccessUrl(String accessUrl);
    List<File> findAllByFileKind(FileKind fileKind);
    List<File> findAllByFileKindAndUploadedBy_Group(FileKind fileKind, Group group);
    Set<File> findAllByFileKindAndUploadedBy(FileKind fileKind, User user);
    boolean existsByFileIdAndFileKind(UUID fileId,FileKind fileKind);
    Optional<File> findByFilename(String filename);
}
