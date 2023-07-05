package com.example.app.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="_event")

public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID eventId;
    private String eventDescription;
    private String eventBody;
    private String eventCreator;
    private LocalDateTime eventDateTime;
    private LocalDateTime eventExpiration;

    @JsonIgnore
    @ManyToMany(mappedBy = "userHasEvents", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @Builder.Default
    //we have a set of users that one event have all the users
    private Set<User> usersJoinInEvent = new HashSet<>();

    public void addUser(User user){
        usersJoinInEvent.add(user);
        user.getUserHasEvents().add(this);
    }
}
