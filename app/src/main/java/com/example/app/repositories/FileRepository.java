package com.example.app.repositories;

import com.example.app.entities.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileRepository extends JpaRepository<File, UUID> {
}
