package com.example.app.models;

import com.example.app.entities.Event;
import com.example.app.entities.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateEventResponse {
    private String eventCreator;
    private String eventBody;
    private String eventDescription;
    private LocalDateTime eventDateTime;
    private LocalDateTime eventExpiration;

    @Builder.Default
    private Set<User> eventJoinInUser = new HashSet<>();
}
