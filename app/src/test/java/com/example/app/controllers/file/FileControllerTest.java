package com.example.app.controllers.file;

import com.example.app.advice.ApplicationExceptionHandler;
import com.example.app.config.TestSecurityConfig;
import com.example.app.controllers.FileController;
import com.example.app.controllers.utils.ExcelFileGenerator;
import com.example.app.entities.FileKind;
import com.example.app.models.responses.common.UserWithFiles;
import com.example.app.models.responses.file.AdminHrManagerFileResponse;
import com.example.app.models.responses.file.FileResourceResponse;
import com.example.app.models.responses.file.FileResponseEntity;
import com.example.app.models.responses.file.UserFileResponse;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.services.FileStorageService;
import com.example.app.utils.file.FileSizeConverter;
import com.example.app.utils.file.FileContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes = {FileController.class,TestSecurityConfig.class, ApplicationExceptionHandler.class})
public class FileControllerTest {

    @MockBean
    private FileStorageService fileStorageService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should upload a multipart file")
    void shouldUploadAMultipartFile() throws Exception {
        String fileContent = "this text is from a test .txt file";
        String fileName = "testFile";
        String token = "testToken";
        byte[] contentBytes = fileContent.getBytes(StandardCharsets.UTF_8);
        var request = new MockMultipartFile("file", fileName, "text/plain", contentBytes);
        var response = UserFileResponse.builder()
                .fileId(UUID.randomUUID())
                .filename(request.getOriginalFilename())
                .fileSize(FileSizeConverter.convert(request.getSize()))
                .fileKind(FileKind.TIMESHEET)
                .build();
        when(fileStorageService.upload(request,any(Principal.class))).thenReturn(response);
        this.mockMvc.perform(multipart("/file/upload").file(request)
                        .header("Authorization", token))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Should Download A Specific Evaluation")
    void shouldDownloadASpecificEvaluation() throws Exception {
        String fileContent = "this text is from a test .txt file";
        String fileName = "testFile";
        String testToken = "testToken";
        byte[] contentBytes = fileContent.getBytes(StandardCharsets.UTF_8);
        File testFile = File.createTempFile(fileName, ".txt");
        FileUtils.writeByteArrayToFile(testFile, contentBytes);
        var response = FileResourceResponse.builder()
                .fileName(fileName)
                .fileType(FileContent.txt.getFileContent())
                .resource(new FileSystemResource(testFile.getAbsolutePath()))
                .build();
        var uuidOfFile = UUID.randomUUID();
        when(fileStorageService.download(uuidOfFile, FileKind.EVALUATION, any(Principal.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/file/download/evaluation/{fileId}",uuidOfFile))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, equalTo("attachment; filename=" + response.getFileName())))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, equalTo(response.getFileType())))
                .andExpect(jsonPath("$", equalTo(response.getResource().getContentAsString(StandardCharsets.UTF_8))));

    }

    @Test
    @DisplayName("Should Download A Specified Timesheet")
    void shouldDownloadASpecifiedTimesheet() throws Exception {
        String fileName = "testFile";
        String testToken = "testToken";
        byte[] contentBytes = ExcelFileGenerator.generateExcelFile(fileName);
        File testFile = File.createTempFile(fileName, ".xls");
        assert contentBytes != null;
        FileUtils.writeByteArrayToFile(testFile, contentBytes);
        var response = FileResourceResponse.builder()
                .fileName(testFile.getName())
                .fileType(FileContent.xls.getFileContent())
                .resource(new FileSystemResource(testFile.getAbsolutePath()))
                .build();
        var uuidOfFile = UUID.randomUUID();
        when(fileStorageService.download(uuidOfFile,FileKind.TIMESHEET,any(Principal.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/file/download/timesheet/{fileId}", UUID.randomUUID()).header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, equalTo("attachment; filename=" + response.getFileName())))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, equalTo(response.getFileType())))
                .andExpect(jsonPath("$.*",is(notNullValue())));
    }


    @Test
    @DisplayName("Should return all evaluations")
    void readAllEvaluation() throws Exception {
        Set<FileResponseEntity> expectedResponse = new HashSet<>();
        for(int i=1;i<6;i++){
            expectedResponse.add(
                     Instancio.of(UserFileResponse.class)
                            .set(field("user"),Instancio.create(AdminUserResponse.class))
                            .set(field("files"),Instancio.createList(AdminHrManagerFileResponse.class))
                            .create());
        }
        when(fileStorageService.readAll(FileKind.EVALUATION)).thenReturn(Set.copyOf(expectedResponse));
        this.mockMvc.perform(MockMvcRequestBuilders.get("/file/evaluation/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()",equalTo(expectedResponse.size())))
                .andExpect(jsonPath("$.size()",greaterThan(0)));
    }

    @Test
    @DisplayName("Should return all evaluations")
    void readAllTimesheet() throws Exception {
        Set<FileResponseEntity> expectedResponse = new HashSet<>();
        for(int i=1;i<6;i++){
            expectedResponse.add(
                    Instancio.of(UserWithFiles.class)
                            .set(field("user"),Instancio.create(AdminUserResponse.class))
                            .set(field("files"),Instancio.createList(AdminHrManagerFileResponse.class))
                            .create());
        }
        when(fileStorageService.readAll(FileKind.TIMESHEET)).thenReturn(expectedResponse);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/file/timesheet/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()",equalTo(expectedResponse.size())))
                .andExpect(jsonPath("$.size()",greaterThan(0)));
    }

    @Test
    @DisplayName("Should delete a file")
    void delete() throws Exception {
        when(fileStorageService.delete(any(UUID.class))).thenReturn(true);
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/file/delete/{fileId}",UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should approve a specified evaluation")
    void approveEvaluation() throws Exception {
        var expectedResponse = Instancio.of(AdminHrManagerFileResponse.class)
                .set(field("uploadDate"), LocalDateTime.of(2023,5,28,12,0,1))
                .set(field("approvedDate"), LocalDateTime.of(2023,5,28,12,0,1))
                .create();
        when(fileStorageService.approveEvaluation(eq(expectedResponse.getFileId()))).thenReturn(expectedResponse);
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/file/approveEvaluation/{fileId}",expectedResponse.getFileId()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.fileId",equalTo(expectedResponse.getFileId().toString())))
                .andExpect(jsonPath("$.filename",equalTo(expectedResponse.getFilename())))
                .andExpect(jsonPath("$.fileSize",equalTo(expectedResponse.getFileSize())))
                .andExpect(jsonPath("$.fileType",equalTo(expectedResponse.getFileType())))
                .andExpect(jsonPath("$.uploadDate",equalTo(expectedResponse.getUploadDate().toString())))
                .andExpect(jsonPath("$.approved",equalTo(expectedResponse.getApproved())))
                .andExpect(jsonPath("$.approvedBy",equalTo(expectedResponse.getApprovedBy())))
                .andExpect(jsonPath("$.approvedDate",equalTo(expectedResponse.getApprovedDate().toString())))
                .andExpect(jsonPath("$.fileKind",equalTo(expectedResponse.getFileKind().name())))
                .andExpect(jsonPath("$.uploadedBy",equalTo(expectedResponse.getUploadedBy())));
    }
}
