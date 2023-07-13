package com.example.app.repositories;

import com.example.app.entities.File;
import com.example.app.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<File, UUID> {
    List<File> findAllByUploadedBy(User uploadedBy);
}
