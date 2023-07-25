package com.example.app.controllers;

import com.example.app.entities.File;
import com.example.app.exception.IllegalTypeOfFileException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.responses.FileStorageProperties;
import com.example.app.repositories.FileRepository;
import com.example.app.services.FileStorageService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final FileStorageService fileStorageService;
    private final FileRepository fileRepo;
    @PostMapping("/upload")
    public ResponseEntity<FileStorageProperties> upload(@RequestBody MultipartFile file,
    @RequestHeader(HttpHeaders.AUTHORIZATION) String token) throws UserNotFoundException,
            IllegalTypeOfFileException {
            return new ResponseEntity<>(fileStorageService.upload(file,token), HttpStatus.CREATED);
    }
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("fileId") UUID fileId) {
            return new ResponseEntity<>(fileStorageService.download(fileId),HttpStatus.OK);
    }
    @GetMapping("/")
    public ResponseEntity<List<File>> readAll(@RequestParam("userId") UUID userId) {
            return new ResponseEntity<>(fileStorageService.readAll(userId),HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<FileStorageProperties> delete(@RequestParam("fileId") UUID fileId) {
        fileStorageService.delete(fileId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}