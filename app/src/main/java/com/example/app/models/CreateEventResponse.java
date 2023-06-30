package com.example.app.models;

import com.example.app.entities.User;

import java.util.List;

public class CreateEventResponse {
    private String eventDescription;
    private String eventBody;
    private String eventCreator;
    private String eventDateTime;
    private List<User> eventUsers;
}
