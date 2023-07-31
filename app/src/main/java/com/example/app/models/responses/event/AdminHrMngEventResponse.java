package com.example.app.models.responses.event;

import com.example.app.entities.User;
import com.example.app.models.responses.user.AdminUserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class AdminHrMngEventResponse implements EventResponseEntity{
    private UUID eventId;
    private String eventDescription;
    private String eventBody;
    private String eventCreator;
    private LocalDateTime eventDateTime;
    private LocalDateTime eventExpiration;
    //must remove
    @Builder.Default
    private Set<AdminUserResponse> users = new HashSet<>();
}
