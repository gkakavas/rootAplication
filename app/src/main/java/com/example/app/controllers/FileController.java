package com.example.app.controllers;

import com.example.app.entities.File;
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
    public ResponseEntity<FileStorageProperties> create(@RequestBody @NotNull MultipartFile file,
                                                        @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        var response = fileStorageService.upload(file, token);
        if (response != null)
            return new ResponseEntity<>(response, HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
    }
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("fileId") UUID fileId) {
        var response = fileStorageService.download(fileId);
        if (response != null)
            return new ResponseEntity<>(response,HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
    }
    @GetMapping("")
    public ResponseEntity<List<File>> readAll(@RequestParam("userId") UUID userId) {
        var response = fileStorageService.readAll(userId);
        if (response != null)
            return new ResponseEntity<>(response,HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
    }

    @DeleteMapping("/delete")
    public ResponseEntity delete(@RequestParam("fileId") UUID fileId) {
        if (fileRepo.existsById(fileId)) {
            var response = fileStorageService.delete(fileId);
            if (response == true)
                return new ResponseEntity(HttpStatus.ACCEPTED);
        }
            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
    }
}