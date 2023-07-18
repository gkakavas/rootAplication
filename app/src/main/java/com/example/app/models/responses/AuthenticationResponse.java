package com.example.app.models.responses;

import lombok.*;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class AuthenticationResponse {
    private String token;
}
