package com.example.app.services;

import com.example.app.models.requests.FileRequestEntity;
import com.example.app.models.responses.FileResponseEntity;

import java.util.List;
import java.util.UUID;

public class FileUploadService implements CrudService<FileResponseEntity, FileRequestEntity> {


    @Override
    public FileResponseEntity create(FileRequestEntity fileRequestEntity, String creatorEmail) {
        return null;
    }

    @Override
    public FileResponseEntity read(UUID id) {
        return null;
    }

    @Override
    public List<FileResponseEntity> read() {
        return null;
    }

    @Override
    public FileResponseEntity update(UUID id, FileRequestEntity fileRequestEntity) {
        return null;
    }

    @Override
    public boolean delete(UUID id) {
        return false;
    }
}
