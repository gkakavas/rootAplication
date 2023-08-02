package com.example.app.controllers;

import com.example.app.entities.File;
import com.example.app.entities.FileKind;
import com.example.app.entities.User;
import com.example.app.exception.FileNotFoundException;
import com.example.app.exception.IllegalTypeOfFileException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.responses.common.UserWithFiles;
import com.example.app.models.responses.file.FileResponseEntity;
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
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final FileStorageService fileStorageService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/upload")
    public ResponseEntity<FileResponseEntity> upload(@RequestBody MultipartFile file,
                                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String token) throws UserNotFoundException,
            IllegalTypeOfFileException, IOException {
            return new ResponseEntity<>(fileStorageService.upload(file,token), HttpStatus.CREATED);
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER','ROLE_USER')")
    @GetMapping("/download/evaluation/{fileId}")
    public ResponseEntity<Resource> downloadEvaluation(@PathVariable("fileId") UUID fileId) throws UserNotFoundException, FileNotFoundException {
            return new ResponseEntity<>(fileStorageService.download(fileId,FileKind.EVALUATION),HttpStatus.OK);
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_HR','ROLE_USER')")
    @GetMapping("/download/timesheet/{fileId}")
    public ResponseEntity<Resource> downloadTimesheet(@PathVariable("fileId") UUID fileId) throws UserNotFoundException, FileNotFoundException {
        return new ResponseEntity<>(fileStorageService.download(fileId,FileKind.TIMESHEET),HttpStatus.OK);
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER','ROLE_USER')")
    @GetMapping("/evaluation/")
    public ResponseEntity<Set<UserWithFiles>> readAllEvaluation() throws UserNotFoundException {
        return new ResponseEntity<>(fileStorageService.readAll(FileKind.EVALUATION),HttpStatus.OK);
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_HR','ROLE_USER')")
    @GetMapping("/timesheet/")
    public ResponseEntity<Set<UserWithFiles>> readAllTimesheet() throws UserNotFoundException {
        return new ResponseEntity<>(fileStorageService.readAll(FileKind.TIMESHEET),HttpStatus.OK);
    }

    @PreAuthorize("hasRole('User')")
    @DeleteMapping("/delete")
    public ResponseEntity<FileResponseEntity> delete(@RequestParam("fileId") UUID fileId) {
        fileStorageService.delete(fileId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}