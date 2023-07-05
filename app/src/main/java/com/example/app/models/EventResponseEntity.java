package com.example.app.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventResponseEntity {
    private UUID eventId;
    private String eventDescription;
    private String eventBody;
    private String eventCreator;
    private LocalDateTime eventDateTime;
    private LocalDateTime eventExpiration;
}
