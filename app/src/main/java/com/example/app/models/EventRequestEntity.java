package com.example.app.models;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestEntity {
    private String eventDescription;
    private String eventBody;
    @Email
    private String eventCreator;
    private LocalDateTime eventDateTime;
    private LocalDateTime eventExpiration;
}
