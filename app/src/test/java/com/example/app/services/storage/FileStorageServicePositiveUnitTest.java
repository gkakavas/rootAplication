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
import com.example.app.utils.converters.file.EntityResponseFileConverterImp;
import com.example.app.entities.FileContent;
import com.example.app.utils.converters.file.FileSizeConverter;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
@ActiveProfiles("unit")
@ContextConfiguration(classes = FileStorageService.class)
public class FileStorageServicePositiveUnitTest {
    @InjectMocks
    private FileStorageService fileStorageService;
    @Mock
    private UserRepository userRepo;
    @Mock
    private FileRepository fileRepo;
    @Mock
    private EntityResponseFileConverterImp fileConverter;
    @Mock
    private FileStorageProperties storageProperties;
    private User currentUser;
    private String roleValue;
    public static final Path rootPath = Path.of("testUploads");
    public static final Path timesheetsPath = rootPath.resolve("timesheets");
    public static final Path evaluationsPath = rootPath.resolve("evaluations");
    public static final java.io.File timesheetFile = Path.of("src/test/resources/testExcelFile.xlsx").toFile();
    public static final java.io.File evaluationFile = Path.of("src/test/resources/testWordFile.docx").toFile();
    public static final FileSystemResource timesheetResource = new FileSystemResource(timesheetFile);
    public static final FileSystemResource evaluationResource = new FileSystemResource(evaluationFile);


    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        fileStorageService = new FileStorageService(fileRepo,fileConverter,storageProperties);
        Files.createDirectories(timesheetsPath);
        Files.createDirectories(evaluationsPath);
        assertTrue(Files.exists(rootPath));
        assertTrue(Files.exists(timesheetsPath));
        assertTrue(Files.exists(evaluationsPath));
    }
    @AfterEach
    void tearDown() throws IOException {
        FileSystemUtils.deleteRecursively(rootPath);
        Assertions.assertFalse(Files.exists(rootPath));
    }

    @BeforeAll
    static void init() throws IOException {

    }
    @AfterAll
    static void tearDownAfterAll() throws IOException {

    }

    void setUpCurrentUser(){
        currentUser = Instancio.of(User.class)
                .set(field(User::getRole),Role.valueOf(roleValue))
                .create();
    }
    @Test
    @DisplayName("Should save a file both filesystem and database")
    void upload() throws IOException, IllegalTypeOfFileException {
        this.roleValue = "USER";
        setUpCurrentUser();
        MultipartFile multipartFile = new  MockMultipartFile(
                timesheetFile.getName(),
                timesheetFile.getName(),
                FileContent.xlsx.getFileContent(),
                timesheetResource.getInputStream());
        var file = File.builder()
                .fileId(UUID.randomUUID()).filename(multipartFile.getOriginalFilename())
                .fileSize(multipartFile.getSize()).fileType(multipartFile.getContentType())
                .uploadDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .accessUrl(Path.of(timesheetsPath.toString(),currentUser.getUserId().toString(), multipartFile.getOriginalFilename()).toString())
                .fileKind(FileKind.TIMESHEET).uploadedBy(currentUser)
                .build();
        var expectedResponse = UserFileResponse.builder()
                .fileId(file.getFileId()).filename(file.getFilename()).fileSize(FileSizeConverter.convert(file.getFileSize()))
                .approved(file.getApproved()).approvedBy("user with this id" + file.getApprovedBy()).approvedDate(file.getApprovedDate())
                .fileKind(file.getFileKind())
                .build();
        when(storageProperties.getTimesheet()).thenReturn(timesheetsPath);
        when(storageProperties.getEvaluation()).thenReturn(evaluationsPath);
        when(fileStorageService.saveInDatabase(multipartFile,currentUser,Path.of(file.getAccessUrl()),file.getFileKind())).thenReturn(expectedResponse);
        var response = fileStorageService.upload(multipartFile,this.currentUser);
        assertEquals(expectedResponse, response);
        assertTrue(Files.exists(Path.of(file.getAccessUrl())));
    }
    @ParameterizedTest
    @CsvSource({
            "ADMIN, EVALUATION",
            "ADMIN, TIMESHEET",
            "HR, TIMESHEET",
            "MANAGER, EVALUATION",
            "USER, EVALUATION",
            "USER, TIMESHEET",
    })
    @DisplayName("Should retrieve a specific file both filesystem and database")
    void download(String roleValue,String fileKind) throws FileNotFoundException, IOException {
        this.roleValue = roleValue;
        setUpCurrentUser();
        User fileUploadedBy = Instancio.create(User.class);
        FileSystemResource resource;
        Path accessUrlBasePath;
        String fileType;
        if(currentUser.getRole().equals(Role.MANAGER)){
            fileUploadedBy.setGroup(currentUser.getGroup());
        }
        else if(currentUser.getRole().equals(Role.USER)){
            fileUploadedBy = currentUser;
        }
        if(fileKind.equals(FileKind.TIMESHEET.name())){
            resource = timesheetResource;
            accessUrlBasePath = timesheetsPath;
            fileType = FileContent.xlsx.getFileContent();
        }
        else{
            resource = evaluationResource;
            accessUrlBasePath = evaluationsPath;
            fileType = FileContent.docx.getFileContent();
        }
        var fileInDB = File.builder()
                .fileId(UUID.randomUUID()).filename(resource.getFilename()).fileSize(resource.contentLength())
                .fileType(fileType).uploadDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .accessUrl(accessUrlBasePath.resolve(Path.of(fileUploadedBy.getUserId().toString(),resource.getFilename())).toString())
                .fileKind(FileKind.valueOf(fileKind)).uploadedBy(fileUploadedBy)
                .build();
        if(currentUser.getRole().equals(Role.USER)){
            currentUser.getUserHasFiles().add(fileInDB);
        }
        FileResponseEntity expectedResponse = FileResourceResponse.builder()
                .fileName(resource.getFilename()).fileType(FileContent.docx.getFileContent()).resource(resource)
                .build();
        when(fileRepo.existsByFileIdAndFileKind(fileInDB.getFileId(), fileInDB.getFileKind())).thenReturn(true);
        when(fileRepo.findById(fileInDB.getFileId())).thenReturn(Optional.of(fileInDB));
        when(fileConverter.fromFileToResource(fileInDB)).thenReturn(expectedResponse);
        var response = fileStorageService.download(fileInDB.getFileId(),FileKind.valueOf(fileKind),this.currentUser);
        assertNotNull(response);
        assertEquals(expectedResponse,response);
    }
    @ParameterizedTest
    @ValueSource(strings = {"TIMESHEET","EVALUATION"})
    @DisplayName("Should return the suitable response of all files in admin response")
    void readAllForAdmin(String fileKind) throws UserNotFoundException {
        this.roleValue = "ADMIN";
        setUpCurrentUser();
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
        var response = fileStorageService.readAll(FileKind.valueOf(fileKind),this.currentUser);
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
        setUpCurrentUser();
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
        var response = fileStorageService.readAll(FileKind.valueOf(fileKind),this.currentUser);
        assertNotNull(response);
        assertEquals(expectedResponse, response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"TIMESHEET","EVALUATION"})
    @DisplayName("Should return the suitable response of user files and for USER role")
    void readAllForUser(String fileKind) throws UserNotFoundException {
        this.roleValue = "USER";
        setUpCurrentUser();
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
        var response = fileStorageService.readAll(FileKind.valueOf(fileKind),this.currentUser);
        assertEquals(expectedResponse, response);
    }

    @Test
    @DisplayName("Should delete a specified file both filesystem and database")
    void delete() throws UserNotFoundException, FileNotFoundException, IOException {
        this.roleValue = "USER";
        setUpCurrentUser();
        Path pathOfFileToDelete = timesheetsPath.resolve(currentUser.getUserId().toString())
                .resolve(Objects.requireNonNull(timesheetResource.getFilename()));
        var fileToDelete = Instancio.of(File.class)
                .set(field(File::getAccessUrl),pathOfFileToDelete.toString())
                .set(field(File::getFileType),FileContent.xlsx.getFileContent())
                .set(field(File::getFileKind),FileKind.TIMESHEET)
                .set(field(File::getUploadedBy),currentUser)
                .create();

        Files.createDirectories(pathOfFileToDelete.getParent());
        Files.copy(timesheetResource.getInputStream(),pathOfFileToDelete);
        when(fileRepo.findById(fileToDelete.getFileId())).thenReturn(Optional.of(fileToDelete));
        var response = fileStorageService.delete(fileToDelete.getFileId());
        assertTrue(response);
        assertFalse(Files.exists(pathOfFileToDelete));
    }

    @Test
    @DisplayName("Should delete all directories")
    void deleteAll() throws IOException {
        if(!Files.exists(rootPath)){
            Files.createDirectories(timesheetsPath);
            Files.createDirectories(evaluationsPath);
        }
        assertTrue(Files.exists(timesheetsPath));
        assertTrue(Files.exists(evaluationsPath));
        when(storageProperties.getRoot()).thenReturn(rootPath);
        fileStorageService.deleteAll();
        assertFalse(Files.exists(rootPath));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN","MANAGER"})
    @DisplayName("Should approve an evaluation document")
    void approveEvaluation(String roleValue) throws FileNotFoundException {
        this.roleValue = roleValue;
        setUpCurrentUser();
        var fileToApprove = Instancio.of(File.class)
                .ignore(field(File::getApprovedBy))
                .ignore(field(File::getApprovedDate))
                .set(field(File::getApproved),false)
                .set(field(File::getFileKind),FileKind.EVALUATION)
                .create();
        if(currentUser.getRole().equals(Role.MANAGER)){
            fileToApprove.getUploadedBy().setGroup(currentUser.getGroup());
        }
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
        var response = fileStorageService.approveEvaluation(fileToApprove.getFileId(),this.currentUser);
        assertNotNull(response);
        assertEquals(expectedResponse,response);

    }

    @Test
    @DisplayName("Should extract multipart info from file save it in database and return the response")
    void saveInDatabase() throws IOException {
        var currentUser = Instancio.of(User.class).create();
        MultipartFile multipartFile = new MockMultipartFile(
                "testExcelFile",
                "testExcelFile.xlsx",
                FileContent.xlsx.getFileContent(),
                timesheetResource.getInputStream());
        var userPath = Path.of(timesheetsPath.resolve(currentUser.getUserId().toString()).toUri());
        var extractedFileEntity = File.builder()
                .fileId(UUID.randomUUID())
                .filename(multipartFile.getOriginalFilename())
                .fileSize(multipartFile.getSize())
                .fileType(multipartFile.getContentType())
                .uploadDate(LocalDateTime.now())
                .accessUrl(userPath.resolve(multipartFile.getOriginalFilename()).toString())
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
        when(fileConverter.extractMultipartInfo(
                multipartFile,
                currentUser,
                userPath.resolve(Objects.requireNonNull(multipartFile.getOriginalFilename())).toString(),
                extractedFileEntity.getFileKind()
                )).thenReturn(extractedFileEntity);
        when(fileRepo.save(extractedFileEntity)).thenReturn(extractedFileEntity);
        when(fileConverter.fromFileToUser(extractedFileEntity)).thenReturn(expectedResponse);
        var response = fileStorageService.saveInDatabase(
                multipartFile,
                currentUser,
                userPath,
                extractedFileEntity.getFileKind());
        assertNotNull(response);
        assertEquals(expectedResponse,response);
    }
}
