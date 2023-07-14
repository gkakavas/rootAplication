package com.example.app.services;

import com.example.app.entities.File;
import com.example.app.models.responses.FileStorageProperties;
import com.example.app.repositories.FileRepository;
import com.example.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {
    //
    private final Path root = Paths.get("uploads");
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final FileRepository fileRepo;

    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    public FileStorageProperties upload(MultipartFile file, String token) {
        try {
            var user = userRepo.findByEmail(jwtService.extractUsername
                    (token.substring(7))).orElse(null);
            try {
                var userPath = Files.createDirectories(this.root.resolve(user.getUserId().toString()));
                Files.copy(file.getInputStream(), userPath.resolve(file.getOriginalFilename()));
            } catch (Exception e) {
                if (e instanceof FileAlreadyExistsException) {
                    throw new RuntimeException("A file of that name already exists.");
                }
                throw new RuntimeException(e.getMessage());
            }
            var newFile = File.builder()
                    .filename(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .accessUrl(this.root.resolve(user.getUserId().toString())
                            .resolve(file.getOriginalFilename()).toString())
                    .uploadDate(LocalDateTime.now())
                    .uploadedBy(user)
                    .build();
            var response = fileRepo.save(newFile);
            return FileStorageProperties
                    .builder()
                    .fileId(response.getFileId())
                    .filename(response.getFilename())
                    .fileSize(response.getFileSize())
                    .fileType(response.getFileType())
                    .accessUrl(response.getAccessUrl())
                    .uploadDate(response.getUploadDate())
                    .uploadedBy(response.getUploadedBy())
                    .build();
        }catch(Exception e){
            if(e instanceof SQLException)
                throw new RuntimeException("Not found user with this email");
        }
       return null;
    }

    public Resource download(UUID fileId) {
        var file = fileRepo.findById(fileId).orElseThrow(()->new RuntimeException("Not Found file with this id"));
            try {
                Path pathToFile = Path.of(file.getAccessUrl());
                Resource resource = new UrlResource(pathToFile.toUri());
                if (resource.exists() || resource.isReadable()) {
                    return resource;
                }
                else {
                    throw new RuntimeException("Could not read the file!");
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException("Error: " + e.getMessage());
            }
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }


    public List<File> readAll(UUID userId) {
        var files = fileRepo.findAllByUploadedBy(userRepo.findById(userId).orElse(null));
        if(!files.isEmpty()){
            return files;
        }
        else
            throw new RuntimeException("This user has not files");
    }

    public boolean delete(UUID fileId) {
        var file = fileRepo.findById(fileId);
        Path pathOfFile = Path.of(file.get().getAccessUrl());
        try {
            Files.delete(pathOfFile);
            fileRepo.deleteById(fileId);
            return true;
        }catch(IOException|IllegalArgumentException e){
            e.printStackTrace();
            return false;
        }
    }
}
