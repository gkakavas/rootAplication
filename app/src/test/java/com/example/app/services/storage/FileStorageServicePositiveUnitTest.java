package com.example.app.services.storage;


import com.example.app.config.FileStorageProperties;
import com.example.app.entities.File;
import com.example.app.entities.FileKind;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.FileNotFoundException;
import com.example.app.exception.IllegalTypeOfFileException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.responses.file.AdminHrManagerFileResponse;
import com.example.app.models.responses.file.FileResourceResponse;
import com.example.app.models.responses.file.FileResponseEntity;
import com.example.app.models.responses.file.UserFileResponse;
import com.example.app.repositories.FileRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.services.FileStorageService;
import com.example.app.tool.utils.ExcelFileGenerator;
import com.example.app.utils.common.EntityResponseCommonConverterImpl;
import com.example.app.utils.file.EntityResponseFileConverterImp;
import com.example.app.utils.file.FileContent;
import com.example.app.utils.file.FileSizeConverter;
import com.example.app.utils.user.EntityResponseUserConverterImpl;
import org.instancio.Instancio;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@ActiveProfiles("unit")
@ContextConfiguration(classes = FileStorageProperties.class)
public class FileStorageServicePositiveUnitTest {
    @InjectMocks
    private FileStorageService fileStorageService;
    @Mock
    private UserRepository userRepo;
    @Mock
    private FileRepository fileRepo;
    @Mock
    private EntityResponseCommonConverterImpl commonConverter;
    @Mock
    private EntityResponseUserConverterImpl userConverter;
    @Mock
    private EntityResponseFileConverterImp fileConverter;
    private static final FileStorageProperties storageProperties = new FileStorageProperties();
    private User currentUser;
    private Object roleValue;
    private Principal principal;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        fileStorageService = new FileStorageService(fileRepo,fileConverter,storageProperties);
    }
    @AfterEach
    void tearDown() {

    }

    @BeforeAll
    static void init() throws IOException {
        Files.createDirectories(storageProperties.getTimesheet());
        Files.createDirectories(storageProperties.getEvaluation());
        assertTrue(Files.exists(storageProperties.getRoot()));
        assertTrue(Files.exists(storageProperties.getTimesheet()));
        assertTrue(Files.exists(storageProperties.getEvaluation()));
    }
    @AfterAll
    static void tearDownAfterAll() throws IOException {
        FileSystemUtils.deleteRecursively(storageProperties.getRoot());
        Assertions.assertFalse(Files.exists(storageProperties.getRoot()));
    }

    void setUpPrincipal(){
        currentUser = Instancio.of(User.class)
                .set(field(User::getRole),roleValue)
                .create();
        principal = (Principal) currentUser;
    }
    @Test
    @DisplayName("Should save a file both filesystem and database")
    void upload() throws IOException, IllegalTypeOfFileException {
        this.roleValue = "USER";
        setUpPrincipal();
        var testExcelFile = ExcelFileGenerator.generateExcelFile();
        MultipartFile multipartFile;
        try(InputStream stream = new FileInputStream(testExcelFile);){
            multipartFile = new  MockMultipartFile(testExcelFile.getName(), testExcelFile.getName(),FileContent.xls.getFileContent(),stream);
        }
        var file = File.builder()
                .fileId(UUID.randomUUID()).filename(multipartFile.getOriginalFilename())
                .fileSize(multipartFile.getSize()).fileType(multipartFile.getContentType())
                .uploadDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .accessUrl("testUploads/timesheet/" +currentUser.getUserId() + "/" + multipartFile.getOriginalFilename())
                .fileKind(FileKind.TIMESHEET).uploadedBy(currentUser)
                .build();
        var expectedResponse = UserFileResponse.builder()
                .fileId(file.getFileId()).filename(file.getFilename()).fileSize(FileSizeConverter.convert(file.getFileSize()))
                .approved(file.getApproved()).approvedBy("user with this id" + file.getApprovedBy()).approvedDate(file.getApprovedDate())
                .fileKind(file.getFileKind())
                .build();
        when(fileStorageService.saveInDatabase(multipartFile,currentUser,Path.of(file.getAccessUrl()),FileKind.TIMESHEET)).thenReturn(expectedResponse);
        var response = fileStorageService.upload(multipartFile,this.principal);
        assertEquals(expectedResponse, response);
        assertTrue(Files.exists(Path.of(file.getAccessUrl())));
    }
    @Test
    @DisplayName("Should retrieve a specific file both filesystem and database")
    void download() throws FileNotFoundException {
        this.roleValue = "USER";
        setUpPrincipal();
        java.io.File file = Path.of("testWordFile.docx").toFile();
        var fileInDB = File.builder()
                .fileId(UUID.randomUUID()).filename(file.getName()).fileSize(file.length())
                .fileType(FileContent.docx.getFileContent()).uploadDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .accessUrl(Path.of("testUploads/evaluations/" + currentUser.getUserId() + "/" + file.getName()).toString())
                .fileKind(FileKind.EVALUATION).uploadedBy(currentUser)
                .build();
        FileResponseEntity expectedResponse = FileResourceResponse.builder()
                .fileName(file.getName()).fileType(FileContent.docx.getFileContent()).resource(new FileSystemResource(file))
                .build();
        when(fileRepo.findById(fileInDB.getFileId())).thenReturn(Optional.of(fileInDB));
        fileConverter.fromFileToResource(fileInDB);
        var response = fileStorageService.download(fileInDB.getFileId(),FileKind.EVALUATION,this.principal);
        assertNotNull(response);
        assertEquals(expectedResponse,response);

    }
    @ParameterizedTest
    @ValueSource(strings = {"TIMESHEET","EVALUATION"})
    @DisplayName("Should return the suitable response of all files in admin response")
    void readAllForAdmin(String fileKind) throws UserNotFoundException {
        this.roleValue = "ADMIN";
        setUpPrincipal();
        var fileKindTestObject = FileKind.valueOf(fileKind);
        var filesInDB = Instancio.stream(File.class)
                .peek(file -> file.setFileKind(fileKindTestObject))
                .limit(20)
                .toList();
        var expectedResponse = filesInDB.stream().map(file -> (FileResponseEntity) AdminHrManagerFileResponse.builder()
                        .fileId(file.getFileId()).filename(file.getFilename())
                        .fileSize(FileSizeConverter.convert(file.getFileSize())).fileType(file.getFileType())
                        .uploadDate(file.getUploadDate()).approved(file.getApproved())
                        .approvedBy("user with id " + file.getApprovedBy()).approvedDate(file.getApprovedDate())
                        .fileKind(file.getFileKind()).uploadedBy(file.getUploadedBy().getEmail())
                        .build())
                .toList();
        when(fileRepo.findAllByFileKind(fileKindTestObject)).thenReturn(filesInDB);
        when(fileConverter.fromFileListToAdminList(Set.copyOf(filesInDB))).thenReturn(expectedResponse);
        var response = fileStorageService.readAll(FileKind.EVALUATION,this.principal);
        assertEquals(expectedResponse, response);
    }
    @ParameterizedTest
    @CsvSource({
            "EVALUATION,MANAGER",
            "TIMESHEET,HR"
    })
    @DisplayName("Should return the suitable response of all files and for manager user")
    void readAllForManager(String fileKind,String roleValue) throws UserNotFoundException {
        this.roleValue = roleValue;
        setUpPrincipal();
        var fileKindTestObject = FileKind.valueOf(fileKind);
        var filesInDB = Instancio.stream(File.class)
                .peek(file -> file.setFileKind(fileKindTestObject))
                .limit(20)
                .toList();
        if(currentUser.getRole().equals(Role.MANAGER)){
            filesInDB.forEach(file-> file.getUploadedBy().setGroup(currentUser.getGroup()));
        }
        var expectedResponse = filesInDB.stream().map(file -> AdminHrManagerFileResponse.builder()
                        .fileId(file.getFileId()).filename(file.getFilename())
                        .fileSize(FileSizeConverter.convert(file.getFileSize())).fileType(file.getFileType())
                        .uploadDate(file.getUploadDate()).approved(file.getApproved())
                        .approvedBy("user with id " + file.getApprovedBy()).approvedDate(file.getApprovedDate())
                        .fileKind(file.getFileKind()).uploadedBy(file.getUploadedBy().getEmail())
                        .build())
                .toList();
        when(fileRepo.findAllByFileKind(FileKind.TIMESHEET)).thenReturn(filesInDB);
        when(fileRepo.findAllByFileKindAndUploadedBy_Group(FileKind.EVALUATION,currentUser.getGroup())).thenReturn(filesInDB);
        when(fileConverter.fromFileListToAdminList(Set.copyOf(filesInDB))).thenReturn(List.copyOf(expectedResponse));
        var response = fileStorageService.readAll(FileKind.valueOf(fileKind),this.principal);
        assertNotNull(response);
        assertEquals(expectedResponse, response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"TIMESHEET","EVALUATION"})
    @DisplayName("Should return the suitable response of user files and for USER role")
    void readAllForUser(String fileKind) throws UserNotFoundException {
        this.roleValue = "USER";
        setUpPrincipal();
        var fileKindTestObject = FileKind.valueOf(fileKind);
        var filesInDB = Instancio.stream(File.class)
                .peek(file -> file.setFileKind(fileKindTestObject))
                .limit(20)
                .toList();
        var expectedResponse = filesInDB.stream().map(file -> (FileResponseEntity) UserFileResponse.builder()
                        .fileId(file.getFileId()).filename(file.getFilename())
                        .fileSize(FileSizeConverter.convert(file.getFileSize())).approved(file.getApproved())
                        .fileKind(file.getFileKind())
                        .approvedBy("user with id " + file.getApprovedBy()).approvedDate(file.getApprovedDate())
                        .build())
                .toList();
        when(fileRepo.findAllByFileKindAndUploadedBy(fileKindTestObject,currentUser)).thenReturn(Set.copyOf(filesInDB));
        when(fileConverter.fromFileListToUserFileList(Set.copyOf(filesInDB))).thenReturn(expectedResponse);
        var response = fileStorageService.readAll(FileKind.EVALUATION,this.principal);
        assertEquals(expectedResponse, response);
    }

    @Test
    @DisplayName("Should delete a specified file both filesystem and database")
    void delete() throws UserNotFoundException, FileNotFoundException, java.io.FileNotFoundException {
        this.roleValue = "USER";
        setUpPrincipal();
        java.io.File file = Path.of("testExcelFile.xlsx").toFile();
        Path pathOfFileToDelete = Path.of("testUploads/timesheets/").resolve(currentUser.getUserId().toString()).resolve(file.getName());
        var fileToDelete = Instancio.of(File.class)
                .set(field(File::getAccessUrl),pathOfFileToDelete)
                .set(field(File::getFileType),FileContent.xlsx.getFileContent())
                .set(field(File::getFileKind),FileKind.TIMESHEET)
                .set(field(File::getUploadedBy),currentUser)
                .create();
        try(InputStream stream = new FileInputStream(file)){
            Files.copy(stream,pathOfFileToDelete);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        when(fileRepo.findById(any(UUID.class))).thenReturn(Optional.of(fileToDelete));
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        var response = fileStorageService.delete(fileToDelete.getFileId());
        assertTrue(response);
    }

    @Test
    @DisplayName("Should delete all directories")
    void deleteAll() throws IOException {
        if(!Files.exists(storageProperties.getRoot())){
            Files.createDirectories(storageProperties.getEvaluation());
            Files.createDirectories(storageProperties.getTimesheet());
        }
        assertTrue(Files.exists(storageProperties.getEvaluation()));
        assertTrue(Files.exists(storageProperties.getTimesheet()));
        fileStorageService.deleteAll();
        Assertions.assertFalse(Files.exists(storageProperties.getRoot()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","MANAGER"})
    @DisplayName("Should approve an evaluation document")
    void approveEvaluation(String roleValue) throws FileNotFoundException {
        this.roleValue = roleValue;
        setUpPrincipal();
        var fileToApprove = Instancio.of(File.class)
                .ignore(field(File::getApprovedBy))
                .ignore(field(File::getApprovedDate))
                .set(field(File::getApproved),false)
                .set(field(File::getFileKind),FileKind.EVALUATION)
                .create();
        var approvedFile = File.builder()
                .fileId(fileToApprove.getFileId()).filename(fileToApprove.getFilename()).fileSize(fileToApprove.getFileSize())
                .fileType(fileToApprove.getFileType()).uploadDate(fileToApprove.getUploadDate()).accessUrl(fileToApprove.getAccessUrl())
                .approved(true).approvedBy(currentUser.getUserId()).approvedDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .fileKind(fileToApprove.getFileKind()).uploadedBy(fileToApprove.getUploadedBy())
                .build();
        var expectedResponse = AdminHrManagerFileResponse.builder()
                .fileId(approvedFile.getFileId()).filename(approvedFile.getFilename())
                .fileSize(FileSizeConverter.convert(approvedFile.getFileSize())).fileType(approvedFile.getFileType())
                .uploadDate(approvedFile.getUploadDate()).approved(approvedFile.getApproved())
                .approvedBy("user with id " + approvedFile.getApprovedBy()).approvedDate(approvedFile.getApprovedDate())
                .fileKind(approvedFile.getFileKind()).uploadedBy(approvedFile.getUploadedBy().getEmail())
                .build();
        when(fileRepo.findById(fileToApprove.getFileId())).thenReturn(Optional.of(fileToApprove));
        when(fileConverter.approveFile(fileToApprove,currentUser)).thenReturn(approvedFile);
        when(fileRepo.save(approvedFile)).thenReturn(approvedFile);
        when(fileConverter.fromFileToAdmin(approvedFile)).thenReturn(expectedResponse);
        var response = fileStorageService.approveEvaluation(fileToApprove.getFileId(),this.principal);
        assertNotNull(response);
        Assertions.assertEquals(expectedResponse,response);

    }

    @Test
    @DisplayName("Should extract multipart info from file save it in database and return the response")
    void saveInDatabase() throws IOException {
        var currentUser = Instancio.of(User.class).create();
        MultipartFile multipartFile = new MockMultipartFile("testExcelFile", "testExcelFile.xlsx",FileContent.xlsx.getFileContent(), new FileInputStream("C:\\Users\\georgios.kakavas\\Downloads\\rootAplication\\app\\src\\test\\testResources\\testExcelFile.xlsx"));
        var extractedFileEntity = File.builder()
                .fileId(UUID.randomUUID())
                .filename(multipartFile.getOriginalFilename())
                .fileSize(multipartFile.getSize())
                .fileType(multipartFile.getContentType())
                .uploadDate(LocalDateTime.now())
                .accessUrl(multipartFile.getResource().toString())
                .fileKind(FileKind.TIMESHEET)
                .uploadedBy(currentUser)
                .build();
        var expectedResponse = UserFileResponse.builder()
                .fileId(extractedFileEntity.getFileId())
                .filename(extractedFileEntity.getFilename())
                .fileSize(FileSizeConverter.convert(extractedFileEntity.getFileSize()))
                .approved(null)
                .approvedBy(null)
                .approvedDate(null)
                .fileKind(extractedFileEntity.getFileKind())
                .build();
        when(fileConverter.extractMultipartInfo(any(MultipartFile.class),any(User.class),any(String.class),any(FileKind.class)))
                .thenReturn(extractedFileEntity);
        when(fileRepo.save(extractedFileEntity)).thenReturn(extractedFileEntity);
        when(fileConverter.fromFileToUser(extractedFileEntity)).thenReturn(expectedResponse);
        var response = fileStorageService.saveInDatabase(multipartFile,currentUser,Path.of(multipartFile.getResource().toString()),FileKind.TIMESHEET);
        assertNotNull(response);
        Assertions.assertEquals(expectedResponse,response);
    }
}
