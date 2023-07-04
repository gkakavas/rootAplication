package com.example.app.models;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateEventRequest {
    private String eventDescription;
    private String eventBody;
    private String eventCreator;
    private LocalDateTime eventDateTime;
    private LocalDateTime eventExpiration;
}
