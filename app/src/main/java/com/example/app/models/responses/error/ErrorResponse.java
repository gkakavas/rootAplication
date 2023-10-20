package com.example.app.models.responses.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse<T> {
    private T message;
    private HttpStatusCode responseCode;
}
