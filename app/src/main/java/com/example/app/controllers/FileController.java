package com.example.app.controllers;

import com.example.app.entities.File;
import com.example.app.entities.FileKind;
import com.example.app.entities.User;
import com.example.app.exception.FileNotFoundException;
import com.example.app.exception.IllegalTypeOfFileException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.responses.common.UserWithFiles;
import com.example.app.models.responses.file.FileResourceResponse;
import com.example.app.models.responses.file.FileResponseEntity;
import com.example.app.models.responses.file.FileStorageProperties;
import com.example.app.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final FileStorageService fileStorageService;


    @PostMapping("/upload")
    public ResponseEntity<FileResponseEntity> upload(@RequestBody MultipartFile file,
                                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String token) throws UserNotFoundException,
            IllegalTypeOfFileException, IOException {
            return new ResponseEntity<>(fileStorageService.upload(file,token), HttpStatus.CREATED);
    }

    @GetMapping("/download/evaluation/{fileId}")
    public ResponseEntity<Resource> downloadEvaluation(@PathVariable UUID fileId) throws UserNotFoundException, FileNotFoundException, IOException {
        FileResourceResponse response = (FileResourceResponse) fileStorageService.download(fileId,FileKind.EVALUATION);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + response.getFileName());
        headers.add(HttpHeaders.CONTENT_TYPE, response.getFileType());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(response.getResource().contentLength())
                .body(response.getResource());
    }

    @GetMapping("/download/timesheet/{fileId}")
    public ResponseEntity<Resource> downloadTimesheet(@PathVariable UUID fileId) throws UserNotFoundException, FileNotFoundException, IOException {
        FileResourceResponse response = (FileResourceResponse) fileStorageService.download(fileId,FileKind.TIMESHEET);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + response.getFileName());
        headers.add(HttpHeaders.CONTENT_TYPE, response.getFileType());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(response.getResource().contentLength())
                .body(response.getResource());
    }

    @GetMapping("/evaluation/")
    public ResponseEntity<Set<UserWithFiles>> readAllEvaluation() throws UserNotFoundException {
        return new ResponseEntity<>(fileStorageService.readAll(FileKind.EVALUATION),HttpStatus.OK);
    }

    @GetMapping("/timesheet/")
    public ResponseEntity<Set<UserWithFiles>> readAllTimesheet() throws UserNotFoundException {
        return new ResponseEntity<>(fileStorageService.readAll(FileKind.TIMESHEET),HttpStatus.OK);
    }

    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<FileResponseEntity> delete(@PathVariable("fileId") UUID fileId)
            throws FileNotFoundException {
        fileStorageService.delete(fileId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/approveEvaluation/{fileId}")
    public ResponseEntity<FileResponseEntity> approveEvaluation(
            @PathVariable("fileId") UUID fileId , Principal principal) throws FileNotFoundException, UserNotFoundException {
        return new ResponseEntity<>(fileStorageService.approveEvaluation(fileId,principal),HttpStatus.ACCEPTED);
    }
}