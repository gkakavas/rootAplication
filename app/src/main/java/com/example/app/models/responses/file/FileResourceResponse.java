package com.example.app.models.responses.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.FileSystemResource;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileResourceResponse implements FileResponseEntity{
    private FileSystemResource resource;
    private String fileType;
    private String fileName;
}
