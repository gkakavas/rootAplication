package com.example.app.controllers;

import com.example.app.advice.ApplicationExceptionHandler;
import com.example.app.config.TestSecurityConfig;
import com.example.app.controllers.utils.ExcelFileGenerator;
import com.example.app.entities.FileKind;
import com.example.app.models.responses.file.FileResourceResponse;
import com.example.app.models.responses.file.FileResponseEntity;
import com.example.app.models.responses.file.UserFileResponse;
import com.example.app.services.FileStorageService;
import com.example.app.utils.FileSizeConverter;
import com.example.app.utils.file.FileContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.in;
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
        var request = new MockMultipartFile("file",fileName,"text/plain",contentBytes);
        var response = Instancio.of(UserFileResponse.class)
                .set(field("filename"),request.getOriginalFilename())
                .set(field("fileSize"), FileSizeConverter.convert(request.getSize()))
                .ignore(field("approved"))
                .ignore(field("approvedBy"))
                .ignore(field("approvedDate"))
                .set(field("fileKind"),FileKind.EVALUATION)
                .create();
        when(fileStorageService.upload(request,token)).thenReturn(response);
        this.mockMvc.perform(multipart("/file/upload").file(request)
                        .header("Authorization",token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileId",equalTo(response.getFileId().toString())))
                .andExpect(jsonPath("$.filename",equalTo(response.getFilename())))
                .andExpect(jsonPath("$.fileSize",equalTo(response.getFileSize())))
                .andExpect(jsonPath("$.approved",equalTo(response.getApproved())))
                .andExpect(jsonPath("$.approvedBy",equalTo(response.getApprovedBy())))
                .andExpect(jsonPath("$.approvedDate",equalTo(response.getApprovedDate())))
                .andExpect(jsonPath("$.fileKind",equalTo(response.getFileKind().name())));
    }

   @Test
    @DisplayName("Should Download A Specified Evaluation")
    void shouldDownloadASpecifiedEvaluation() throws Exception {
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
       when(fileStorageService.download(any(UUID.class),eq(FileKind.EVALUATION))).thenReturn(response);
       this.mockMvc.perform(MockMvcRequestBuilders.get("/file/download/evaluation/{fileId}",UUID.randomUUID()).header("Authorization",testToken))
               .andExpect(status().isOk())
               .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,equalTo("attachment; filename=" + response.getFileName())))
               .andExpect(header().string(HttpHeaders.CONTENT_TYPE,equalTo(response.getFileType())))
               .andExpect(jsonPath("$",equalTo(response.getResource().getContentAsString(StandardCharsets.UTF_8))));

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
                .fileName(fileName)
                .fileType(FileContent.xls.getFileContent())
                .resource(new FileSystemResource(testFile.getAbsolutePath()))
                .build();
        when(fileStorageService.download(any(UUID.class),eq(FileKind.TIMESHEET))).thenReturn(response);
        InputStream inputStream = response.getResource().getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        this.mockMvc.perform(MockMvcRequestBuilders.get("/file/download/timesheet/{fileId}",UUID.randomUUID()).header("Authorization",testToken))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,equalTo("attachment; filename=" + response.getFileName())))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,equalTo(response.getFileType())))
                .andExpect(jsonPath("$",equalTo(bufferedReader.readLine()).));
        /*System.out.println(content().toString());
        System.out.println(response.getResource().getFile().toString());*/



    }


    /*@Test
    @DisplayName()
    void readAllEvaluation(){

        when(fileStorageService.download(testFileId,FileKind.TIMESHEET)).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/file/download/timesheet/{fileId}",testFileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",equalTo(response.getContentAsString(StandardCharsets.UTF_8))));
    }
    @Test
    @DisplayName()
    void readAllTimesheet(){

    }
    @Test
    @DisplayName()
    void delete(){

    }
    @Test
    @DisplayName()
    void approveEvaluation(){*/


}
