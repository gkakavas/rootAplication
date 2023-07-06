package com.example.app.models;

import com.example.app.entities.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseEntity {
    private UUID userId;
    private String firstname;
    private String lastname;
    private String email;
    private String specialization;
    @Builder.Default
    private Set<Event> events = new HashSet<>();
}
