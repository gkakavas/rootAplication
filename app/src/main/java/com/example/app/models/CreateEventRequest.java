package com.example.app.models;

import com.example.app.entities.User;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class CreateEventRequest {
    private String eventDescription;
    private String eventBody;
    private String eventCreator;
    private String eventDateTime;
    private List<User> users;
}
