package com.example.app.converters.file;

import com.example.app.entities.File;
import com.example.app.entities.FileKind;
import com.example.app.entities.User;
import com.example.app.models.responses.file.AdminHrManagerFileResponse;
import com.example.app.models.responses.file.FileResourceResponse;
import com.example.app.models.responses.file.UserFileResponse;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.file.FileSizeConverter;
import com.example.app.utils.file.EntityResponseFileConverterImp;
import com.example.app.utils.file.FileContent;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@ActiveProfiles("unit")
public class FileConverterPositiveUnitTest {
    @InjectMocks
    private EntityResponseFileConverterImp fileConverter;
    @Mock
    private UserRepository userRepo;
    com.example.app.entities.File fileToConvert = Instancio.of(File.class)
            .create();
    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        fileConverter = new EntityResponseFileConverterImp(userRepo);

    }

    @Test
    @DisplayName("Should convert a File entity to AdminHrManagerFileResponse")
    void shouldConvertAFileEntityToAdminHrManagerFileResponse(){
        var approvedBy = Instancio.create(User.class);
        when(userRepo.findById(any(UUID.class))).thenReturn(Optional.of(approvedBy));
        var expectedResponse = AdminHrManagerFileResponse.builder()
                .fileId(fileToConvert.getFileId())
                .filename(fileToConvert.getFilename())
                .fileSize(FileSizeConverter.convert(fileToConvert.getFileSize()))
                .fileType(fileToConvert.getFileType())
                .uploadDate(fileToConvert.getUploadDate())
                .approved(fileToConvert.getApproved())
                .approvedBy(approvedBy.getEmail())
                .approvedDate(fileToConvert.getApprovedDate())
                .fileKind(fileToConvert.getFileKind())
                .uploadedBy(fileToConvert.getUploadedBy().getEmail())
                .build();
        var response = fileConverter.fromFileToAdmin(fileToConvert);
        Assertions.assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should convert a File entity to UserFileResponse")
    void shouldConvertAFileEntityToUserFileResponse(){
        var approvedBy = Instancio.create(User.class);
        when(userRepo.findById(any(UUID.class))).thenReturn(Optional.of(approvedBy));
        var expectedResponse = UserFileResponse.builder()
                .fileId(fileToConvert.getFileId())
                .filename(fileToConvert.getFilename())
                .fileSize(FileSizeConverter.convert(fileToConvert.getFileSize()))
                .approved(fileToConvert.getApproved())
                .approvedBy(approvedBy.getEmail())
                .approvedDate(fileToConvert.getApprovedDate())
                .fileKind(fileToConvert.getFileKind())
                .build();
        var response = fileConverter.fromFileToUser(fileToConvert);
        Assertions.assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should retrieve a resource from file system from a File entity")
    void shouldRetrieveAResourceFromFileSystemFromAFileEntity() throws IOException {
        Path temp = Files.createTempFile("testFile", ".docx");
        java.io.File file = new java.io.File(temp.toUri());
        fileToConvert.setFilename(file.getName());
        fileToConvert.setFileType(FileContent.docx.getFileContent());
        fileToConvert.setAccessUrl(file.getAbsolutePath());
        var expectedResponse = FileResourceResponse.builder()
                .fileName(fileToConvert.getFilename())
                .fileType(fileToConvert.getFileType())
                .resource(new FileSystemResource(file.getAbsolutePath()))
                .build();
        var response = fileConverter.fromFileToResource(fileToConvert);
        Assertions.assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should convert a file entity list to AdminHrManagerFileResponse list")
    void shouldConvertAFileEntityListToAdminHrManagerFileResponseList(){
        var fileListSize = 4;
        var approveUser = Instancio.create(User.class);
        var fileSet = Instancio.ofSet(File.class)
                .size(fileListSize)
                .create();
        var expectedResponse = fileSet.stream()
                .map(file -> AdminHrManagerFileResponse.builder()
                        .fileId(file.getFileId())
                        .filename(file.getFilename())
                        .fileSize(FileSizeConverter.convert(file.getFileSize()))
                        .fileType(file.getFileType())
                        .uploadDate(file.getUploadDate())
                        .approved(file.getApproved())
                        .approvedBy(approveUser.getEmail())
                        .approvedDate(file.getApprovedDate())
                        .fileKind(file.getFileKind())
                        .uploadedBy(file.getUploadedBy().getEmail())
                        .build()
                )
                .toList();
        when(userRepo.findById(any(UUID.class))).thenReturn(Optional.of(approveUser));
        var response = fileConverter.fromFileListToAdminList(fileSet);
        Assertions.assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should convert a file entity list to UserFileResponse list")
    void shouldConvertAFileEntityListToUserFileResponseList(){
        var fileListSize = 4;
        var approveUser = Instancio.create(User.class);
        var fileSet = Instancio.ofSet(File.class)
                .size(fileListSize)
                .create();
        var expectedResponse = fileSet.stream()
                .map(file -> UserFileResponse.builder()
                        .fileId(file.getFileId())
                        .filename(file.getFilename())
                        .fileSize(FileSizeConverter.convert(file.getFileSize()))
                        .approved(file.getApproved())
                        .approvedBy(approveUser.getEmail())
                        .approvedDate(file.getApprovedDate())
                        .fileKind(file.getFileKind())
                        .build()
                )
                .toList();
        when(userRepo.findById(any(UUID.class))).thenReturn(Optional.of(approveUser));
        var response = fileConverter.fromFileListToUserFileList(fileSet);
        Assertions.assertEquals(expectedResponse,response);
    }
    @Test
    @DisplayName("Should extract info from a multipart file input and then creating file entity with these info")
    void shouldExtractInfoFromAMultipartFileInputAndThenCreatingFileEntity() throws IOException {
        MultipartFile multipartFile;
        var fileCreator = Instancio.create(User.class);
        var fileKind = FileKind.TIMESHEET;
        try(InputStream stream = new FileInputStream("C:\\Users\\georgios.kakavas\\Downloads\\rootAplication\\app\\src\\test\\testResources\\testExcelFile.xlsx")){
            multipartFile = new MockMultipartFile("testExcelFile", "testExcelFile.xlsx",FileContent.xlsx.getFileContent(), stream);
        }
        var accessUrl = multipartFile.getResource().toString();
        var expectedResponse = File.builder()
                .filename(multipartFile.getOriginalFilename())
                .fileSize(multipartFile.getSize())
                .fileType(multipartFile.getContentType())
                .uploadDate(LocalDateTime.now())
                .accessUrl(accessUrl)
                .fileKind(fileKind)
                .uploadedBy(fileCreator)
                .build();
        var response = fileConverter.extractMultipartInfo(multipartFile,fileCreator,accessUrl,fileKind);
        Assertions.assertEquals(expectedResponse,response);
    }

    @Test
    @DisplayName("Should approve an existing file entity")
    void shouldApproveAnExistingFileEntity(){
        var file = Instancio.of(File.class)
                .ignore(field("approved"))
                .ignore(field("approvedBy"))
                .ignore(field("approvedDate"))
                .create();
        var user = Instancio.create(User.class);
        var expectedResponse = File.builder()
                .fileId(file.getFileId())
                .filename(file.getFilename())
                .fileSize(file.getFileSize())
                .fileType(file.getFileType())
                .uploadDate(file.getUploadDate())
                .accessUrl(file.getAccessUrl())
                .approved(true)
                .approvedBy(user.getUserId())
                .approvedDate(LocalDateTime.now())
                .fileKind(file.getFileKind())
                .uploadedBy(file.getUploadedBy())
                .build();
        var response = fileConverter.approveFile(file,user);
        response.setApprovedDate(expectedResponse.getApprovedDate());
        Assertions.assertEquals(expectedResponse,response);
    }

}
