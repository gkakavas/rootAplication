package com.example.app.models.requests;

import com.example.app.entities.User;
import com.example.app.utils.validator.date.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
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
public class EventRequestEntity implements RequestEntity {
    @NotBlank(message = "Event description is required")
    @Size(min = 5,max=100,message = "Event description should contain at least 5 and no many than 100 characters including spaces")
    private String eventDescription;
    @NotBlank(message = "Event body is required")
    @Size(min = 100,message = "Event body should contain at least 100 characters including spaces")
    private String eventBody;
    @NotBlank(message = "Event date and time is required")
    @DateTime(message = "Invalid event date and time format. The correct format is yyyy-MM-ddTHH:mm:ss")
    private String eventDateTime;
    @NotBlank(message = "Event expiration is required")
    @DateTime(message = "Invalid event expiration format. The correct format is yyyy-MM-ddTHH:mm:ss")
    private String eventExpiration;
    @Builder.Default
    private Set<UUID> idsSet = new HashSet<>();
}
