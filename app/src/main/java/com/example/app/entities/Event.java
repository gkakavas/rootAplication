package com.example.app.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="_event")

public class Event {
    @Id
    @GeneratedValue
    private Integer eventId;
    private String eventDescription;
    private String eventBody;
    private String eventCreator;
    private String eventDateTime;

    @ManyToMany(mappedBy = "userHasEvents")
    //@Builder.Default
    //we have a set of users that one event have all the users
    private final Set<User> userSet = new HashSet<>();
}
