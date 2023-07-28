package com.example.app.controllers;

import com.example.app.entities.File;
import com.example.app.exception.IllegalTypeOfFileException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.responses.file.FileStorageProperties;
import com.example.app.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final FileStorageService fileStorageService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/upload")
    public ResponseEntity<FileStorageProperties> upload(@RequestBody MultipartFile file,
    @RequestHeader(HttpHeaders.AUTHORIZATION) String token) throws UserNotFoundException,
            IllegalTypeOfFileException, IOException {
            return new ResponseEntity<>(fileStorageService.upload(file,token), HttpStatus.CREATED);
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER','ROLE_USER')")
    @GetMapping("/download/evaluation/{fileId}")
    public ResponseEntity<Resource> downloadEvaluation(@PathVariable("fileId") UUID fileId) {
            return new ResponseEntity<>(fileStorageService.download(fileId),HttpStatus.OK);
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_HR','ROLE_USER')")
    @GetMapping("/download/timesheet/{fileId}")
    public ResponseEntity<Resource> downloadTimesheet(@PathVariable("fileId") UUID fileId) {
        return new ResponseEntity<>(fileStorageService.download(fileId),HttpStatus.OK);
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER','ROLE_USER')")
    @GetMapping("/evaluation/")
    public ResponseEntity<List<File>> readAllEvaluation(@RequestParam("userId") UUID userId) {
        return new ResponseEntity<>(fileStorageService.readAll(userId),HttpStatus.OK);
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_HR','ROLE_USER')")
    @GetMapping("/timesheet/")
    public ResponseEntity<List<File>> readAllTimesheet(@RequestParam("userId") UUID userId) {
        return new ResponseEntity<>(fileStorageService.readAll(userId),HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<FileStorageProperties> delete(@RequestParam("fileId") UUID fileId) {
        fileStorageService.delete(fileId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}