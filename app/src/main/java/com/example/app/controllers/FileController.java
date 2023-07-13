package com.example.app.controllers;

import com.example.app.models.requests.FileRequestEntity;
import com.example.app.models.responses.FileStorageProperties;
import com.example.app.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController   {
private final FileStorageService fileStorageService;

@PostMapping("/upload")
    public ResponseEntity<FileStorageProperties> create(@RequestParam("file") MultipartFile file,
                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
    var response = fileStorageService.upload(file,token);
    if (response!=null)
        return new ResponseEntity<>(response, HttpStatus.OK);
    else
        return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
    }
}
