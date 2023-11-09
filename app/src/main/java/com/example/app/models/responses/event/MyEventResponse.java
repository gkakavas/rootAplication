package com.example.app.models.responses.event;

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
public class MyEventResponse implements EventResponseEntity{
    private UUID eventId;
    private String eventDescription;
    private String eventBody;
    private LocalDateTime eventDateTime;
    private LocalDateTime eventExpiration;
}
