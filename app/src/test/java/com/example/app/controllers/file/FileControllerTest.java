package com.example.app.controllers.file;

import com.example.app.advice.ApplicationExceptionHandler;
import com.example.app.config.TestSecurityConfig;
import com.example.app.controllers.FileController;
import com.example.app.entities.FileKind;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.models.responses.file.AdminHrManagerFileResponse;
import com.example.app.models.responses.file.FileResourceResponse;
import com.example.app.models.responses.file.UserFileResponse;
import com.example.app.services.FileStorageService;
import com.example.app.tool.utils.ExcelFileGenerator;
import com.example.app.utils.file.FileContent;
import com.example.app.utils.file.FileSizeConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ActiveProfiles("unit")
@WebMvcTest
@ContextConfiguration(classes = {FileController.class,TestSecurityConfig.class, ApplicationExceptionHandler.class})
public class FileControllerTest {

    @MockBean
    private FileStorageService fileStorageService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private String roleValue;
    private User currentUser;

    void setUp(){
        this.currentUser = Instancio.of(User.class)
                .set(field(User::getRole), Role.valueOf(roleValue))
                .create();
    }
    @Test
    @DisplayName("Should upload a multipart file")
    void shouldUploadAMultipartFile() throws Exception {
        String fileContent = "this text is from a test .txt file";
        String fileName = "testFile";
        byte[] contentBytes = fileContent.getBytes(StandardCharsets.UTF_8);
        var request = new MockMultipartFile("file", fileName, "text/plain", contentBytes);
        var response = UserFileResponse.builder()
                .fileId(UUID.randomUUID())
                .filename(request.getOriginalFilename())
                .fileSize(FileSizeConverter.convert(request.getSize()))
                .fileKind(FileKind.TIMESHEET)
                .build();
        when(fileStorageService.upload(eq(request),nullable(Principal.class))).thenReturn(response);
        this.mockMvc.perform(multipart("/file/upload").file(request))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Should Download A Specific Evaluation")
    void shouldDownloadASpecificEvaluation() throws Exception {
        String fileContent = "this text is from a test .txt file";
        String fileName = "testFile";
        byte[] contentBytes = fileContent.getBytes(StandardCharsets.UTF_8);
        File testFile = File.createTempFile(fileName, ".txt");
        FileUtils.writeByteArrayToFile(testFile, contentBytes);
        var response = FileResourceResponse.builder()
                .fileName(fileName)
                .fileType(FileContent.txt.getFileContent())
                .resource(new FileSystemResource(testFile.getAbsolutePath()))
                .build();
        var uuidOfFile = UUID.randomUUID();
        when(fileStorageService.download(eq(uuidOfFile),eq(FileKind.EVALUATION), nullable(Principal.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/file/download/evaluation/{fileId}",uuidOfFile))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, equalTo("attachment; filename=" + response.getFileName())))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, equalTo(response.getFileType())))
                .andExpect(jsonPath("$", equalTo(response.getResource().getContentAsString(StandardCharsets.UTF_8))));
    }

    @Test
    @DisplayName("Should Download A Specific Timesheet")
    void shouldDownloadASpecifiedTimesheet() throws Exception {
        String fileName = "testFile";
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
        when(fileStorageService.download(eq(uuidOfFile),eq(FileKind.TIMESHEET),nullable(Principal.class))).thenReturn(response);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/file/download/timesheet/{fileId}", uuidOfFile.toString()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, equalTo("attachment; filename=" + response.getFileName())))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, equalTo(response.getFileType())))
                .andExpect(jsonPath("$.*",is(notNullValue())));
    }
    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","MANAGER","USER"})
    @DisplayName("Should return all evaluations")
    void readAllEvaluation(String roleValue) throws Exception {
        this.roleValue = roleValue;
        setUp();
        var adminHrMngResponse = Instancio.ofSet(
                    AdminHrManagerFileResponse.class)
                .generate(field(AdminHrManagerFileResponse::getFileType), gen -> gen
                        .enumOf(FileContent.class)
                        .excluding(FileContent.xls, FileContent.xlsx).asString())
                .generate(field(AdminHrManagerFileResponse::getFileKind), gen -> gen
                        .enumOf(FileKind.class)
                        .excluding(FileKind.TIMESHEET)
                ).create();
        var userResponse = Instancio.ofSet(
                        UserFileResponse.class)
                .generate(field(UserFileResponse::getFileKind), gen -> gen
                        .enumOf(FileKind.class)
                        .excluding(FileKind.TIMESHEET)
                ).create();
        if(List.of("ADMIN","MANAGER").contains(roleValue)) {
            when(fileStorageService.readAll(eq(FileKind.EVALUATION), nullable(Principal.class))).thenReturn(List.copyOf(adminHrMngResponse));
            this.mockMvc.perform(MockMvcRequestBuilders.get("/file/evaluation/all"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(adminHrMngResponse)));
        }
        else{
            when(fileStorageService.readAll(eq(FileKind.EVALUATION), nullable(Principal.class))).thenReturn(List.copyOf(userResponse));
            this.mockMvc.perform(MockMvcRequestBuilders.get("/file/evaluation/all"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(userResponse)));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","HR","USER"})
    @DisplayName("Should return all timesheets")
    void readAllTimesheets(String roleValue) throws Exception {
        this.roleValue = roleValue;
        setUp();
        var adminHrMngResponse = Instancio.ofSet(
                        AdminHrManagerFileResponse.class)
                .generate(field(AdminHrManagerFileResponse::getFileType), gen -> gen
                        .enumOf(FileContent.class)
                        .excluding(FileContent.txt, FileContent.rtf, FileContent.docx).asString())
                .generate(field(AdminHrManagerFileResponse::getFileKind), gen -> gen
                        .enumOf(FileKind.class)
                        .excluding(FileKind.EVALUATION)
                ).create();
        var userResponse = Instancio.ofSet(
                        UserFileResponse.class)
                .generate(field(UserFileResponse::getFileKind), gen -> gen
                        .enumOf(FileKind.class)
                        .excluding(FileKind.EVALUATION)
                ).create();
        if(List.of("ADMIN","HR").contains(roleValue)) {
            when(fileStorageService.readAll(eq(FileKind.TIMESHEET), nullable(Principal.class))).thenReturn(List.copyOf(adminHrMngResponse));
            this.mockMvc.perform(MockMvcRequestBuilders.get("/file/timesheet/all"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(adminHrMngResponse)));
        }
        else{
            when(fileStorageService.readAll(eq(FileKind.TIMESHEET), nullable(Principal.class))).thenReturn(List.copyOf(userResponse));
            this.mockMvc.perform(MockMvcRequestBuilders.get("/file/timesheet/all"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(userResponse)));
        }
    }

    @Test
    @DisplayName("Should delete a file")
    void delete() throws Exception {
        var uuidOfFileToDelete = UUID.randomUUID();
        when(fileStorageService.delete(eq(uuidOfFileToDelete))).thenReturn(true);
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/file/delete/{fileId}",uuidOfFileToDelete.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should approve a specified evaluation")
    void approveEvaluation() throws Exception {
        var approvedFile = Instancio.of(AdminHrManagerFileResponse.class)
                .generate(field(AdminHrManagerFileResponse::getFileType),gen -> gen
                        .enumOf(FileContent.class).excluding(FileContent.xlsx,FileContent.xls).asString()
                )
                .generate(field(AdminHrManagerFileResponse::getFileKind),gen -> gen
                        .enumOf(FileKind.class).excluding(FileKind.TIMESHEET)
                )
                .create();
        when(fileStorageService.approveEvaluation(eq(approvedFile.getFileId()),nullable(Principal.class))).thenReturn(approvedFile);
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/file/approveEvaluation/{fileId}",approvedFile.getFileId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(approvedFile)));
    }
}
