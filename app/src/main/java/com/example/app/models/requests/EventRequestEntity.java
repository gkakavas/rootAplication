package com.example.app.models.requests;

import com.example.app.entities.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestEntity {

    @NotNull(message = "Event Description is required")
    private String eventDescription;
    @NotNull(message = "Event Body is required")
    private String eventBody;
    @NotNull
    @Email
    private String eventCreator;
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDateTime;
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventExpiration;

    @Builder.Default
    private Set<UUID> userIds = new HashSet<>();
}
