package com.example.app.integration.positive;

import com.example.app.config.FileStorageProperties;
import com.example.app.entities.File;
import com.example.app.entities.FileKind;
import com.example.app.entities.User;
import com.example.app.models.responses.common.UserWithFiles;
import com.example.app.models.responses.file.AdminHrManagerFileResponse;
import com.example.app.models.responses.file.FileResourceResponse;
import com.example.app.models.responses.file.FileResponseEntity;
import com.example.app.models.responses.file.UserFileResponse;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.repositories.FileRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.services.JwtService;
import com.example.app.utils.file.FileContent;
import com.example.app.utils.file.FileSizeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FilePositiveIntegrationTest {
    @LocalServerPort
    private int port;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private FileRepository fileRepo;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FileStorageProperties storageProperties;
    private static TestRestTemplate restTemplate;
    private String baseUrl;
    @Container
    public static PostgreSQLContainer<?> myPostgresContainer = new PostgreSQLContainer<>("postgres:13.11")
            .withCommand("postgres", "-c", "log_statement=all");
    private String currentToken;
    private File evaluationToDownload;
    private File timesheetToDownload;
    private String roleValue;
    private User currentUser;
    private static HttpHeaders headers;

    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", myPostgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", myPostgresContainer::getUsername);
        registry.add("spring.datasource.password", myPostgresContainer::getPassword);
    }
    @BeforeAll
    public static void init() {
        myPostgresContainer.start();
        restTemplate = new TestRestTemplate();
        headers = new HttpHeaders();
    }

    public void setUp() {
        baseUrl = "http://localhost:".concat(String.valueOf(port).concat("/file"));
        var filesToDownload = userRepo.findByEmail("firstname7@email.com").orElseThrow().getUserHasFiles();
        this.evaluationToDownload = filesToDownload.stream().filter(file -> file.getFileKind().equals(FileKind.EVALUATION)).findFirst().orElseThrow();
        this.timesheetToDownload = filesToDownload.stream().filter(file -> file.getFileKind().equals(FileKind.TIMESHEET)).findFirst().orElseThrow();

        switch (this.roleValue) {
            case "ADMIN" -> {
                currentUser = userRepo.findByEmail("firstname1@email.com").orElseThrow();
                currentToken = jwtService.generateToken(currentUser);
            }
            case "HR" -> {
                currentUser = userRepo.findByEmail("firstname4@email.com").orElseThrow();
                currentToken = jwtService.generateToken(currentUser);
            }
            case "MANAGER" -> {
                currentUser = userRepo.findByEmail("firstname3@email.com").orElseThrow();
                currentToken = jwtService.generateToken(currentUser);
            }
            case "USER" -> {
                currentUser = userRepo.findByEmail("firstname7@email.com").orElseThrow();
                currentToken = jwtService.generateToken(currentUser);
            }
        }
        headers.set("Authorization", "Bearer " + currentToken);
    }

    @BeforeEach
    void beforeEachSetup() throws IOException {
        Files.createDirectories(storageProperties.getEvaluation());
        Files.createDirectories(storageProperties.getTimesheet());
    }
    @AfterEach
    void tearDown () throws IOException {
        SecurityContextHolder.clearContext();
        FileSystemUtils.deleteRecursively(storageProperties.getRoot());
    }

    @AfterAll
    public static void afterAll () {
        myPostgresContainer.stop();
    }
    @ParameterizedTest
    @CsvSource({
            "ADMIN, timesheet",
            "ADMIN, evaluation",
            "USER, timesheet",
            "USER, evaluation"
    })
    @DisplayName("When an upload file request is dispatched " +
            "should save the file in database and filesystem and return response to user")
    void shouldSaveTheFileInDatabaseAndFilesystemAndReturnResponse(String roleValue,String fileToUpload) throws IOException {
        this.roleValue = roleValue;
        setUp();
        baseUrl = baseUrl.concat("/upload");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        Resource resource;
        if(fileToUpload.equals("timesheet")){
            resource = new ClassPathResource("testUploadExcelFile.xlsx");
        }
        else{
            resource = new ClassPathResource("testUploadWordFile.docx");
        }
        body.add("file", resource);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<UserFileResponse>() {});
        var fileEntity = fileRepo.findById(actualResponse.getFileId()).orElseThrow();
        var fileContent = Files.readAllBytes(Paths.get(fileEntity.getAccessUrl()));
        MultipartFile multipartFile = new MockMultipartFile(Objects.requireNonNull(resource.getFilename()),fileContent);
        var expectedAdminResponse = UserFileResponse.builder()
                .fileId(actualResponse.getFileId())
                .filename(resource.getFilename())
                .fileSize(FileSizeConverter.convert(multipartFile.getSize()))
                .fileKind(fileEntity.getFileKind())
                .build();
        var createdFile = fileRepo.findById(actualResponse.getFileId()).orElseThrow();
        assertEquals(response.getStatusCode(),HttpStatus.CREATED);
        assertEquals(expectedAdminResponse,actualResponse);
        assertTrue(Files.exists(Path.of(createdFile.getAccessUrl()), LinkOption.NOFOLLOW_LINKS));
        fileRepo.deleteById(createdFile.getFileId());
        FileSystemUtils.deleteRecursively(Path.of(createdFile.getUploadedBy().getUserId().toString()).toFile());
        assertFalse(Files.exists(Path.of(createdFile.getUploadedBy().getUserId().toString())));
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "MANAGER", "USER"})
    @DisplayName("When a download evaluation request is dispatched " +
            "should retrieve the file from database and filesystem and return response with file and information about file to user")
    void evaluationDownloadShouldRetrieveTheFileFromDatabaseAndFilesystemAndReturnTheFileWithSuitableHeadersThatIncludingTheFileMetadata(String roleValue) throws IOException {
        this.roleValue = roleValue;
        setUp();
        preDownloadSetup();
        baseUrl = baseUrl.concat("/download/evaluation/").concat(this.evaluationToDownload.getFileId().toString());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        var expectedResponse = FileResourceResponse.builder()
                .resource(new FileSystemResource(this.evaluationToDownload.getAccessUrl()))
                .fileType(this.evaluationToDownload.getFileType())
                .fileName(this.evaluationToDownload.getFilename())
                .build();
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(MediaType.valueOf(expectedResponse.getFileType()),response.getHeaders().getContentType());
        assertEquals(ContentDisposition.attachment().filename(expectedResponse.getFileName())
                .build(),response.getHeaders().getContentDisposition());
        assertNotNull(response.getBody());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "HR", "USER"})
    @DisplayName("When a download timesheet request is dispatched " +
            "should retrieve the file from database and filesystem and return response with file and information about file to user")
     void timesheetDownloadShouldRetrieveTheFileFromDatabaseAndFilesystemAndReturnTheFileWithSuitableHeadersThatIncludingTheFileMetadata(String roleValue) throws IOException {
        this.roleValue = roleValue;
        setUp();
        preDownloadSetup();
        baseUrl = baseUrl.concat("/download/timesheet/").concat(this.timesheetToDownload.getFileId().toString());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        var expectedResponse = FileResourceResponse.builder()
                .resource(new FileSystemResource(this.timesheetToDownload.getAccessUrl()))
                .fileType(this.timesheetToDownload.getFileType())
                .fileName(this.timesheetToDownload.getFilename())
                .build();
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(MediaType.valueOf(expectedResponse.getFileType()),response.getHeaders().getContentType());
        assertEquals(ContentDisposition.attachment().filename(expectedResponse.getFileName())
                .build(),response.getHeaders().getContentDisposition());
        assertNotNull(response.getBody());
    }


    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "HR", "USER"})
    @DisplayName("When a read all timesheets request is dispatched " +
            "should retrieve all timesheets from database and return them to user")
    void shouldRetrieveAllTimesheetsFromDatabaseAndReturnThemToUser(String roleValue) throws JsonProcessingException {
        this.roleValue = roleValue;
        setUp();
        baseUrl = baseUrl.concat("/timesheet/all");
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        if(roleValue.equals("ADMIN")||roleValue.equals("HR")){
            var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<Set<AdminHrManagerFileResponse>>() {});
            var files = fileRepo.findAllByFileKind(FileKind.TIMESHEET);
            var expectedResponse = new HashSet<AdminHrManagerFileResponse>();
            for (File file:files){
                expectedResponse.add(AdminHrManagerFileResponse.builder()
                        .fileId(file.getFileId())
                        .filename(file.getFilename())
                        .fileSize(FileSizeConverter.convert(file.getFileSize()))
                        .fileType(file.getFileType())
                        .uploadDate(file.getUploadDate())
                        .fileKind(file.getFileKind())
                        .uploadedBy(file.getUploadedBy().getEmail())
                        .build());
            }
            assertEquals(expectedResponse,actualResponse);
            assertEquals(response.getStatusCode(),HttpStatus.OK);
        }
        else if(roleValue.equals("USER")){
            var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<Set<UserFileResponse>>() {});
            var userEvaluations = currentUser.getUserHasFiles().stream().filter(file -> file.getFileKind().equals(FileKind.TIMESHEET)).toList();
            var expectedResponse = userEvaluations.stream().map(file ->
                    UserFileResponse.builder()
                            .fileId(file.getFileId())
                            .filename(file.getFilename())
                            .fileSize(FileSizeConverter.convert(file.getFileSize()))
                            .fileKind(file.getFileKind())
                            .build())
                    .collect(Collectors.toSet());
            assertEquals(expectedResponse,actualResponse);
            assertEquals(response.getStatusCode(),HttpStatus.OK);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "MANAGER", "USER"})
    @DisplayName("When a read all evaluations request is dispatched " +
            "should retrieve all evaluations from database and return them to user")
    void shouldRetrieveAllEvaluationsFromDatabaseAndReturnThemToUser(String roleValue) throws JsonProcessingException {
        this.roleValue = roleValue;
        setUp();
        baseUrl = baseUrl.concat("/evaluation/all");
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        switch (roleValue) {
            case "ADMIN" -> {
                var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<Set<AdminHrManagerFileResponse>>() {
                });
                var files = fileRepo.findAllByFileKind(FileKind.EVALUATION);
                var expectedResponse = new HashSet<AdminHrManagerFileResponse>();
                for (File file : files) {
                    expectedResponse.add(AdminHrManagerFileResponse.builder()
                            .fileId(file.getFileId())
                            .filename(file.getFilename())
                            .fileSize(FileSizeConverter.convert(file.getFileSize()))
                            .fileType(file.getFileType())
                            .uploadDate(file.getUploadDate())
                            .fileKind(file.getFileKind())
                            .uploadedBy(file.getUploadedBy().getEmail())
                            .build());
                }
                assertEquals(expectedResponse, actualResponse);
                assertEquals(response.getStatusCode(), HttpStatus.OK);
            }
            case "MANAGER" -> {
                var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<Set<AdminHrManagerFileResponse>>() {
                });
                var files = fileRepo.findAllByFileKindAndUploadedBy_Group(FileKind.EVALUATION, currentUser.getGroup());
                var expectedResponse = new HashSet<AdminHrManagerFileResponse>();
                for (File file : files) {
                    expectedResponse.add(AdminHrManagerFileResponse.builder()
                            .fileId(file.getFileId())
                            .filename(file.getFilename())
                            .fileSize(FileSizeConverter.convert(file.getFileSize()))
                            .fileType(file.getFileType())
                            .uploadDate(file.getUploadDate())
                            .fileKind(file.getFileKind())
                            .uploadedBy(file.getUploadedBy().getEmail())
                            .build());
                }
                assertEquals(expectedResponse, actualResponse);
                assertEquals(response.getStatusCode(), HttpStatus.OK);
            }
            case "USER" -> {
                var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<Set<UserFileResponse>>() {
                });
                var userEvaluations = currentUser.getUserHasFiles().stream().filter(file -> file.getFileKind().equals(FileKind.EVALUATION)).toList();
                var expectedResponse = userEvaluations.stream().map(file ->
                                UserFileResponse.builder()
                                        .fileId(file.getFileId())
                                        .filename(file.getFilename())
                                        .fileSize(FileSizeConverter.convert(file.getFileSize()))
                                        .fileKind(file.getFileKind())
                                        .build())
                        .collect(Collectors.toSet());
                assertEquals(expectedResponse, actualResponse);
                assertEquals(response.getStatusCode(), HttpStatus.OK);
            }
        }
    }
    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "USER"})
    @DisplayName("When a delete file request is dispatched " +
            "should delete the file from database and filesystem")
    void shouldDeleteTheFileFromDatabaseAndFilesystem(String roleValue) throws IOException {
        this.roleValue = roleValue;
        setUp();
        Path userUUID = Path.of(this.currentUser.getUserId().toString());
        Path userPath = Files.createDirectories(storageProperties.getEvaluation().resolve(userUUID));
        Resource resource = new ClassPathResource("testUploadWordFile.docx");
        Files.copy(resource.getInputStream(), userPath.resolve(Objects.requireNonNull(resource.getFilename())));
        MultipartFile multipartFile = new MockMultipartFile(
                resource.getFilename(),
                resource.getFilename(),
                FileContent.docx.getFileContent(),
                resource.getInputStream());
        var fileEntity = File.builder()
                .filename(multipartFile.getOriginalFilename())
                .fileSize(multipartFile.getSize())
                .fileType(multipartFile.getContentType())
                .uploadDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .accessUrl(userPath.resolve(resource.getFilename()).toString())
                .fileKind(FileKind.EVALUATION)
                .uploadedBy(currentUser)
                .build();
        var existingFileEntity = fileRepo.save(fileEntity);
        baseUrl = baseUrl.concat("/delete/").concat(existingFileEntity.getFileId().toString());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class);
        assertEquals(HttpStatus.NO_CONTENT,response.getStatusCode());
        assertFalse(Files.exists(Path.of(existingFileEntity.getAccessUrl())));
        assertFalse(fileRepo.existsById(existingFileEntity.getFileId()));
    }
    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "MANAGER"})
    @DisplayName("When an approve evaluation request is dispatched " +
            "should approve the file, save it in database return this file")
    void shouldApproveTheFileSaveItInDatabaseReturnThisFile(String roleValue) throws IOException {
        this.roleValue = roleValue;
        setUp();
        Path userUUID = Path.of(this.currentUser.getUserId().toString());
        Path userPath = Files.createDirectories(storageProperties.getEvaluation().resolve(userUUID));
        Resource resource = new ClassPathResource("testUploadWordFile.docx");
        Files.copy(resource.getInputStream(), userPath.resolve(Objects.requireNonNull(resource.getFilename())));
        MultipartFile multipartFile = new MockMultipartFile(
                resource.getFilename(),
                resource.getFilename(),
                FileContent.docx.getFileContent(),
                resource.getInputStream());
        var fileEntity = File.builder()
                .filename(multipartFile.getOriginalFilename())
                .fileSize(multipartFile.getSize())
                .fileType(multipartFile.getContentType())
                .uploadDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .accessUrl(userPath.resolve(resource.getFilename()).toString())
                .fileKind(FileKind.EVALUATION)
                .uploadedBy(currentUser)
                .build();
        var existingFileEntity = fileRepo.save(fileEntity);
        baseUrl = baseUrl.concat("/approveEvaluation/").concat(existingFileEntity.getFileId().toString());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PATCH,
                new HttpEntity<>(headers),
                String.class);
        var actualResponse = objectMapper.readValue(response.getBody(), new TypeReference<AdminHrManagerFileResponse>() {});
        var expectedResponse = AdminHrManagerFileResponse.builder()
                .fileId(existingFileEntity.getFileId())
                .filename(existingFileEntity.getFilename())
                .fileSize(FileSizeConverter.convert(existingFileEntity.getFileSize()))
                .fileType(existingFileEntity.getFileType())
                .uploadDate(actualResponse.getUploadDate())
                .approved(true)
                .approvedBy(currentUser.getEmail())
                .approvedDate(actualResponse.getApprovedDate())
                .fileKind(existingFileEntity.getFileKind())
                .uploadedBy(existingFileEntity.getUploadedBy().getEmail())
                .build();
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(expectedResponse,actualResponse);
        existingFileEntity.setUploadedBy(null);
        fileRepo.save(existingFileEntity);
        fileRepo.deleteById(existingFileEntity.getFileId());
        assertFalse(fileRepo.existsById(existingFileEntity.getFileId()));
    }


    public void preDownloadSetup() throws IOException {
        var timesheets = fileRepo.findAllByFileKind(FileKind.TIMESHEET);
        var evaluations = fileRepo.findAllByFileKind(FileKind.EVALUATION);
        for (File file : timesheets) {
            Path userUUID = Path.of(file.getUploadedBy().getUserId().toString());
            Path userPath = Files.createDirectories(storageProperties.getTimesheet().resolve(userUUID));
            Resource resource = new ClassPathResource(file.getFilename());
            Files.copy(resource.getInputStream(), userPath.resolve(file.getFilename()));
        }
        for (File file : evaluations) {
            Path userUUID = Path.of(file.getUploadedBy().getUserId().toString());
            Path userPath = Files.createDirectories(storageProperties.getEvaluation().resolve(userUUID));
            Resource resource = new ClassPathResource(file.getFilename());
            Files.copy(resource.getInputStream(), userPath.resolve(file.getFilename()));
        }
    }
}
