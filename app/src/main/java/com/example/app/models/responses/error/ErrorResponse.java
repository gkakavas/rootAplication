package com.example.app.models.responses.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvalidUuidErrorResponse {
    private String message;
    private Integer responseCode;
}
