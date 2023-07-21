package com.example.app.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Event")
@Table(name="_event")

public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID eventId;
    private String eventDescription;
    private String eventBody;
    private UUID eventCreator;
    private LocalDateTime eventDateTime;
    private LocalDateTime eventExpiration;

    @JsonIgnore
    @ManyToMany(mappedBy = "userHasEvents", cascade ={CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<User> usersJoinInEvent = new HashSet<>();
}
