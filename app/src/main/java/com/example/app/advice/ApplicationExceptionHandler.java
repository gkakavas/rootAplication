package com.example.app.advice;

import com.example.app.exception.*;
import com.example.app.models.responses.error.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.postgresql.util.PSQLException;
import java.sql.SQLException;
import java.util.*;

@RestControllerAdvice
public class ApplicationExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<Map<String,String>>> handleInvalidArgument(MethodArgumentNotValidException ex){
        Map<String, String> errorMap = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errorMap.put(error.getField(),error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.<Map<String,String>>builder()
                .message(errorMap)
                .responseCode(HttpStatus.BAD_REQUEST)
                .build());
        }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse<String>> handleUserException(UserNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.<String>builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.NOT_FOUND)
                .build());
    }
    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ErrorResponse<String>> handleEventException(EventNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.<String>builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.NOT_FOUND)
                .build());
    }

    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<ErrorResponse<String>> handleGroupException(GroupNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.<String>builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.NOT_FOUND)
                .build());
    }


    @ExceptionHandler(LeaveNotFoundException.class)
    public ResponseEntity<ErrorResponse<String>> handleUserException(LeaveNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.<String>builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.NOT_FOUND)
                .build());
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponse<String>> handleFileException(FileNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.<String>builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.NOT_FOUND)
                .build());
    }
//    exception when the id is not UUID type
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse<Map<String,String>>> handleConstraintViolation(ConstraintViolationException ex){
        Map<String,String> violationMessages = new HashMap<>();
        for(ConstraintViolation<?> constraintViolation : ex.getConstraintViolations()){
            String propertyPath = constraintViolation.getPropertyPath().toString();
            String[] parts = propertyPath.split("\\.");
            String lastPart = parts[parts.length - 1];
            violationMessages.put(lastPart,constraintViolation.getMessageTemplate());
        }
        return ResponseEntity.badRequest().body(ErrorResponse.<Map<String,String>> builder()
                .message(violationMessages)
                .responseCode(HttpStatus.BAD_REQUEST)
                .build());
    }

    @ExceptionHandler(IllegalTypeOfFileException.class)
    public ResponseEntity<ErrorResponse<String>> handleInvalidTypeOfFile(IllegalTypeOfFileException ex){
        return ResponseEntity.internalServerError().body(ErrorResponse.<String> builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.INTERNAL_SERVER_ERROR)
                .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse<String>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        if(ex.getMessage().contains("Invalid UUID")){
            return ResponseEntity.badRequest().body(ErrorResponse.<String> builder()
                        .message("Invalid path variable's UUID")
                        .responseCode(HttpStatus.BAD_REQUEST)
                    .build());
        }
        else return null;
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse<Map<String,String>>> handleNullRequestBody(HttpMessageNotReadableException ex) {
        String exceptionMessage = ex.getMessage();
        Map<String,String> errorMap = new HashMap<>();
        if (exceptionMessage.contains("UUID has to be represented by standard 36-char representation")) {
            errorMap.put("error","Invalid UUID value provided");
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.<Map<String,String>>builder()
                            .message(errorMap)
                            .responseCode(HttpStatus.BAD_REQUEST)
                            .build());
        }
        else if (exceptionMessage.contains("Required request body is missing")) {
            errorMap.put("error","Request body is required");
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.<Map<String,String>>builder()
                            .message(errorMap)
                            .responseCode(HttpStatus.BAD_REQUEST)
                            .build());
        }
        else {errorMap.put("error","Unexpected error during converting the Json. " +
                "Make sure that response body values URI parameters is correct");
        }
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.<Map<String,String>> builder()
                        .message(errorMap)
                        .responseCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
    }
    @ExceptionHandler(PSQLException.class)
    public ResponseEntity<ErrorResponse<Map<String,String>>> handleSQLException(PSQLException ex){
        String exceptionMessage = ex.getMessage();
        Map<String,String> errorMap = new HashMap<>();
        if (exceptionMessage.contains("duplicate key value violates unique constraint \"_user_email_key\"")) {
            errorMap.put("email","This email already exists");
        }
        else{
            errorMap.put("error","Unexpected SQL exception occurs");
        }
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.<Map<String,String>>builder()
                        .message(errorMap)
                        .responseCode(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }

}
