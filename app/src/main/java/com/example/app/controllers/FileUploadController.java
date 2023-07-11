package com.example.app.controllers;

import com.example.app.models.requests.FileRequestEntity;
import com.example.app.models.responses.FileResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FileUploadController implements CrudController<FileResponseEntity, FileRequestEntity>  {

    @Override
    public ResponseEntity<FileResponseEntity> create(FileRequestEntity request, String header) {
        return null;
    }

    @Override
    public ResponseEntity<FileResponseEntity> readOne(UUID id) {
        return null;
    }

    @Override
    public List<FileResponseEntity> readAll() {
        return null;
    }

    @Override
    public ResponseEntity<FileResponseEntity> update(UUID id, FileRequestEntity fileRequestEntity) {
        return null;
    }

    @Override
    public ResponseEntity<FileResponseEntity> delete(UUID id) {
        return null;
    }
}
