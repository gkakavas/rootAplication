package com.example.app.services.storage;


import com.example.app.config.FileStorageProperties;
import com.example.app.entities.*;
import com.example.app.exception.FileNotFoundException;
import com.example.app.exception.IllegalTypeOfFileException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.responses.common.UserWithFiles;
import com.example.app.models.responses.file.AdminHrManagerFileResponse;
import com.example.app.models.responses.file.FileResourceResponse;
import com.example.app.models.responses.file.FileResponseEntity;
import com.example.app.models.responses.file.UserFileResponse;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;
import com.example.app.repositories.FileRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.services.FileStorageService;
import com.example.app.services.JwtService;
import com.example.app.tool.utils.ExcelFileGenerator;
import com.example.app.utils.file.FileSizeConverter;
import com.example.app.utils.common.EntityResponseCommonConverterImpl;
import com.example.app.utils.file.EntityResponseFileConverterImp;
import com.example.app.utils.file.FileContent;
import com.example.app.utils.user.EntityResponseUserConverterImpl;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.instancio.Instancio;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

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
        fileStorageService = new FileStorageService( fileRepo,fileConverter,storageProperties);
    }
    @AfterEach
    void tearDown() {

    }

    @BeforeAll
    static void init() throws IOException {
        Files.createDirectories(storageProperties.getTimesheet());
        Files.createDirectories(storageProperties.getEvaluation());
        Assertions.assertTrue(Files.exists(storageProperties.getRoot()));
        Assertions.assertTrue(Files.exists(storageProperties.getTimesheet()));
        Assertions.assertTrue(Files.exists(storageProperties.getEvaluation()));
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
        byte[] content = Files.readAllBytes(testExcelFile.toPath());
        MultipartFile multipartFile = new MockMultipartFile();
        var file = Instancio.of(File.class)
                .set(field("filename"),multipartFile.getOriginalFilename())
                .set(field("fileType"),multipartFile.getContentType())
                .create();
        var adminResponse = AdminHrManagerFileResponse.builder()
                .fileId(file.getFileId()).filename(file.getFilename())
                .fileSize(FileSizeConverter.convert(file.getFileSize()))
                .fileType(file.getFileType()).uploadDate(file.getUploadDate())
                .approved(file.getApproved()).approvedBy(currentUser.getEmail())
                .approvedDate(file.getApprovedDate()).fileKind(file.getFileKind())
                .uploadedBy(file.getUploadedBy().getEmail())
                .build();
        var userResponse = UserFileResponse.builder()
                .fileId(file.getFileId())
                .filename(file.getFilename())
                .fileSize(FileSizeConverter.convert(file.getFileSize()))
                .approved(file.getApproved())
                .approvedBy("user with this id" + file.getApprovedBy())
                .approvedDate(file.getApprovedDate())
                .fileKind(file.getFileKind())
                .build();
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        when(fileRepo.findFileByAccessUrl(any(String.class))).thenReturn(file);
        if(List.of(Role.HR,Role.ADMIN,Role.MANAGER).contains(currentUser.getRole())){
            when(fileConverter.fromFileToUser(file)).thenReturn(adminResponse);
        }
        else if(currentUser.getRole().equals(Role.USER)){
            when(fileConverter.fromFileToUser(file)).thenReturn(userResponse);
        }
        when(fileConverter.extractMultipartInfo(any(), any(), any(), any())).thenReturn(file);
        when(fileRepo.save(file)).thenReturn(file);
        var response = fileStorageService.upload(multipartFile);
        if(List.of(Role.HR,Role.ADMIN,Role.MANAGER).contains(currentUser.getRole())){
            Assertions.assertNotNull(response);
            Assertions.assertEquals(adminResponse,response);
        }
        else if(currentUser.getRole().equals(Role.USER)){
            Assertions.assertNotNull(response);
            Assertions.assertEquals(userResponse,response);
        }
    }
    @Test
    @DisplayName("Should retrieve a specific file both filesystem and database")
    void download() throws UserNotFoundException, FileNotFoundException {
        var file = File.builder()
                .fileId(UUID.randomUUID())
                .filename("testExcelFile.xlsx")
                .fileSize(5L)
                .fileType(FileContent.xlsx.getFileContent())
                .accessUrl("C:\\Users\\georgios.kakavas\\Downloads\\rootAplication\\app\\src\\test\\testResources\\testExcelFile.xlsx")
                .fileKind(FileKind.TIMESHEET)
                .build();
        var currentUser = Instancio.create(User.class);
        FileResponseEntity expectedResponse = FileResourceResponse.builder()
                .fileName(file.getFilename())
                .fileType(file.getFileType())
                .resource(new FileSystemResource("C:\\Users\\georgios.kakavas\\Downloads\\rootAplication\\app\\src\\test\\testResources\\testExcelFile.xlsx"))
                .build();
        when(fileRepo.existsByFileIdAndFileKind(any(UUID.class),any(FileKind.class))).thenReturn(true);
        when(fileRepo.findById(any(UUID.class))).thenReturn(Optional.of(file));
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        when(fileConverter.fromFileToResource(file)).thenReturn(expectedResponse);
        var response = fileStorageService.download(file.getFileId(),FileKind.TIMESHEET);
        Assertions.assertEquals(expectedResponse,response);
        Assertions.assertNotNull(response);
    }
    @ParameterizedTest
    @ValueSource(strings = {"TIMESHEET","EVALUATION"})
    @DisplayName("Should return the suitable response of all selected files files and for admin users")
    void readAllForAdmin(String fileKind) throws UserNotFoundException {
        var fileKindTestObject = FileKind.valueOf(fileKind);
        var currentUser = Instancio.of(User.class)
                .set(field("role"), Role.ADMIN)
                .create();
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        var adminFiles = Instancio.stream(File.class)
                .limit(5)
                .peek(file -> file.setFileKind(fileKindTestObject)).toList();
        when(fileRepo.findAllByFileKind(any(FileKind.class))).thenReturn(adminFiles);
        Set<User> users = new HashSet<>();
        Set<UserWithFiles> expectedResponse = new HashSet<>();
        for (File file : adminFiles) {
            users.add(file.getUploadedBy());
        }
        for (User user : users) {
            expectedResponse.add(new UserWithFiles(
                    AdminUserResponse.builder()
                            .userId(user.getUserId()).firstname(user.getFirstname())
                            .lastname(user.getLastname()).email(user.getEmail())
                            .specialization(user.getSpecialization()).currentProject(user.getCurrentProject())
                            .groupName(user.getGroup().getGroupName()).createdBy("user with this id" + user.getCreatedBy())
                            .registerDate(user.getRegisterDate()).lastLogin(user.getLastLogin()).role(user.getRole())
                            .build(), List.copyOf(
                    user.getUserHasFiles().stream().map(file ->
                                    AdminHrManagerFileResponse.builder()
                                            .fileId(file.getFileId())
                                            .filename(file.getFilename())
                                            .fileSize(FileSizeConverter.convert(file.getFileSize()))
                                            .fileType(file.getFileType())
                                            .uploadDate(file.getUploadDate())
                                            .approved(file.getApproved())
                                            .approvedBy("approveBy user with this id" + file.getApprovedBy())
                                            .approvedDate(file.getApprovedDate())
                                            .fileKind(file.getFileKind())
                                            .uploadedBy(file.getUploadedBy().getEmail())
                                            .build())
                            .collect(Collectors.toList())
            )
            ));
            when(commonConverter.usersWithFilesList(anySet())).thenReturn(expectedResponse);
            var response = fileStorageService.readAll(FileKind.EVALUATION);
            Assertions.assertEquals(expectedResponse, response);
        }
    }
    @ParameterizedTest
    @CsvSource({"EVALUATION,MANAGER"})
    @DisplayName("Should return the suitable response of all files and for manager user")
    void readAllForManager(String fileKind,String role) throws UserNotFoundException {
        var fileKindTestObject = FileKind.valueOf(fileKind);
        var roleTestObject = Role.valueOf(role);
        var currentUser = Instancio.of(User.class)
                .set(field("role"), roleTestObject)
                .create();
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        Set<UserWithFiles> expectedResponse = new HashSet<>();
        var group = Instancio.create(Group.class);
            var managerFiles = Instancio.stream(File.class)
                    .limit(5)
                    .peek(file -> {
                        file.setFileKind(fileKindTestObject);
                        file.getUploadedBy().setGroup(group);
                    }).toList();
            when(fileRepo.findAllByFileKindAndUploadedBy_Group(any(FileKind.class),any(Group.class))).thenReturn(managerFiles);
        List<User> userList = managerFiles.stream().map(File::getUploadedBy).distinct().toList();
        for (User user : userList) {
            expectedResponse.add(new UserWithFiles(
                    OtherUserResponse.builder()
                            .userId(user.getUserId())
                            .firstname(user.getFirstname())
                            .lastname(user.getLastname())
                            .email(user.getEmail())
                            .specialization(user.getSpecialization())
                            .currentProject(user.getCurrentProject())
                            .groupName(user.getGroup().getGroupName())
                            .build(), List.copyOf(
                    user.getUserHasFiles().stream().map(file ->
                                    AdminHrManagerFileResponse.builder()
                                            .fileId(file.getFileId())
                                            .filename(file.getFilename())
                                            .fileSize(FileSizeConverter.convert(file.getFileSize()))
                                            .fileType(file.getFileType())
                                            .uploadDate(file.getUploadDate())
                                            .approved(file.getApproved())
                                            .approvedBy("approveBy user with this id" + file.getApprovedBy())
                                            .approvedDate(file.getApprovedDate())
                                            .fileKind(file.getFileKind())
                                            .uploadedBy(file.getUploadedBy().getEmail())
                                            .build())
                            .collect(Collectors.toList())
            )));
        }
        when(commonConverter.usersWithFilesList(anySet())).thenReturn(expectedResponse);
        var response = fileStorageService.readAll(FileKind.EVALUATION);
        Assertions.assertEquals(expectedResponse, response);
    }

    @ParameterizedTest
    @CsvSource({"TIMESHEET,HR"})
    @DisplayName("Should return the suitable response of all files and for HR users")
    void readAllForHR(String fileKind,String role) throws UserNotFoundException {
        var fileKindTestObject = FileKind.valueOf(fileKind);
        var roleTestObject = Role.valueOf(role);
        var currentUser = Instancio.of(User.class)
                .set(field("role"), roleTestObject)
                .create();
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        Set<UserWithFiles> expectedResponse = new HashSet<>();
        var hrFiles = Instancio.stream(File.class)
                .limit(5)
                .peek(file -> file.setFileKind(fileKindTestObject)).toList();
        when(fileRepo.findAllByFileKind(any(FileKind.class))).thenReturn(hrFiles);
        List<User> userList = hrFiles.stream().map(File::getUploadedBy).distinct().toList();
        for (User user : userList) {
            expectedResponse.add(new UserWithFiles(
                    OtherUserResponse.builder()
                            .userId(user.getUserId())
                            .firstname(user.getFirstname())
                            .lastname(user.getLastname())
                            .email(user.getEmail())
                            .specialization(user.getSpecialization())
                            .currentProject(user.getCurrentProject())
                            .groupName(user.getGroup().getGroupName())
                            .build(), List.copyOf(
                    user.getUserHasFiles().stream().map(file ->
                                    AdminHrManagerFileResponse.builder()
                                            .fileId(file.getFileId())
                                            .filename(file.getFilename())
                                            .fileSize(FileSizeConverter.convert(file.getFileSize()))
                                            .fileType(file.getFileType())
                                            .uploadDate(file.getUploadDate())
                                            .approved(file.getApproved())
                                            .approvedBy("approveBy user with this id" + file.getApprovedBy())
                                            .approvedDate(file.getApprovedDate())
                                            .fileKind(file.getFileKind())
                                            .uploadedBy(file.getUploadedBy().getEmail())
                                            .build())
                            .collect(Collectors.toList())
            )));
        }
        when(commonConverter.usersWithFilesList(anySet())).thenReturn(expectedResponse);
        var response = fileStorageService.readAll(FileKind.TIMESHEET);
        Assertions.assertEquals(expectedResponse, response);
    }

    @ParameterizedTest
    @CsvSource({
            "TIMESHEET,USER",
            "EVALUATION,USER"
    })
    @DisplayName("Should return the suitable response of user files and for USER role")
    void readAllForUser(String fileKind,String role) throws UserNotFoundException {
        var fileKindTestObject = FileKind.valueOf(fileKind);
        var roleTestObject = Role.valueOf(role);
        var currentUser = Instancio.of(User.class)
                .set(field("role"), roleTestObject)
                .create();
        currentUser.getUserHasFiles().forEach(file -> file.setFileKind(fileKindTestObject));
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        when(fileRepo.findAllByFileKindAndUploadedBy(any(FileKind.class),any(User.class))).thenReturn(currentUser.getUserHasFiles());
        var otherUserResponse = OtherUserResponse.builder()
                        .userId(currentUser.getUserId())
                        .firstname(currentUser.getFirstname())
                        .lastname(currentUser.getLastname())
                        .email(currentUser.getEmail())
                        .specialization(currentUser.getSpecialization())
                        .currentProject(currentUser.getCurrentProject())
                        .groupName(currentUser.getGroup().getGroupName())
                        .build();
        var userFileList = currentUser.getUserHasFiles().stream().map(file ->
                        UserFileResponse.builder()
                                .fileId(file.getFileId())
                                .filename(file.getFilename())
                                .fileSize(FileSizeConverter.convert(file.getFileSize()))
                                .approved(file.getApproved())
                                .approvedBy("approveBy user with this id" + file.getApprovedBy())
                                .approvedDate(file.getApprovedDate())
                                .fileKind(file.getFileKind())
                                .build())
                .toList();
        var expectedResponse = Set.of(UserWithFiles.builder()
                .user(otherUserResponse)
                .files(List.copyOf(userFileList))
                .build());
        when(userConverter.fromUserToOtherUser(any(User.class))).thenReturn(otherUserResponse);
        when(fileConverter.fromFileListToUserFileList(currentUser.getUserHasFiles())).thenReturn(List.copyOf(userFileList));
        when(commonConverter.usersWithFilesList(anySet())).thenReturn(expectedResponse);
        var response = fileStorageService.readAll(FileKind.TIMESHEET);
        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    @DisplayName("Should delete a specified file both filesystem and database")
    void delete() throws UserNotFoundException, FileNotFoundException {
        var currentUser = Instancio.of(User.class)
                .set(field("role"),Role.valueOf("USER"))
                .create();
        var fileToDelete = Instancio.of(File.class)
                .set(field("accessUrl"),tempTestFile.getAbsolutePath())
                .create();
        fileToDelete.setUploadedBy(currentUser);
        when(fileRepo.findById(any(UUID.class))).thenReturn(Optional.of(fileToDelete));
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        var response = fileStorageService.delete(fileToDelete.getFileId());
        Assertions.assertTrue(response);
    }

    @Test
    @DisplayName("Should delete all directories")
    void deleteAll() throws IOException {
        var testPath = Files.createDirectories(storageProperties.getEvaluation());
        Assertions.assertTrue(Files.exists(testPath));
        fileStorageService.deleteAll();
        Assertions.assertFalse(Files.exists(storageProperties.getRoot()));
    }

    @Test
    @DisplayName("Should approve an evaluation document")
    void approveEvaluation() throws UserNotFoundException, FileNotFoundException {
        var currentUser = Instancio.of(User.class)
                .set(field("role"),Role.valueOf("MANAGER"))
                .create();
        var fileToApprove = Instancio.of(File.class)
                .ignore(field("approved"))
                .ignore(field("approvedBy"))
                .ignore(field("approvedDate"))
                .set(field("fileKind"),FileKind.valueOf("EVALUATION"))
                .create();
        fileToApprove.getUploadedBy().setGroup(currentUser.getGroup());
        var approvedFile = File.builder()
                .fileId(fileToApprove.getFileId())
                .filename(fileToApprove.getFilename())
                .fileSize(fileToApprove.getFileSize())
                .fileType(fileToApprove.getFileType())
                .uploadDate(fileToApprove.getUploadDate())
                .accessUrl(fileToApprove.getAccessUrl())
                .approved(true)
                .approvedBy(currentUser.getUserId())
                .approvedDate(LocalDateTime.now())
                .fileKind(fileToApprove.getFileKind())
                .uploadedBy(fileToApprove.getUploadedBy())
                .build();
        var expectedResponse = AdminHrManagerFileResponse.builder()
                .fileId(approvedFile.getFileId())
                .filename(approvedFile.getFilename())
                .fileSize(FileSizeConverter.convert(approvedFile.getFileSize()))
                .fileType(approvedFile.getFileType())
                .uploadDate(approvedFile.getUploadDate())
                .approved(approvedFile.getApproved())
                .approvedBy(currentUser.getEmail())
                .approvedDate(approvedFile.getApprovedDate())
                .fileKind(approvedFile.getFileKind())
                .uploadedBy(approvedFile.getUploadedBy().getEmail())
                .build();
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(currentUser));
        when(fileRepo.findById(any(UUID.class))).thenReturn(Optional.of(fileToApprove));
        when(fileConverter.approveFile(fileToApprove,currentUser)).thenReturn(approvedFile);
        when(fileRepo.save(approvedFile)).thenReturn(approvedFile);
        when(fileConverter.fromFileToAdmin(approvedFile)).thenReturn(expectedResponse);
        var response = fileStorageService.approveEvaluation(fileToApprove.getFileId());
        Assertions.assertNotNull(response);
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
        Assertions.assertNotNull(response);
        Assertions.assertEquals(expectedResponse,response);
    }
}
