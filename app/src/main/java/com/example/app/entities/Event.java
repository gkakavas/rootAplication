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

    @Builder.Default
    @ManyToMany(mappedBy = "userHasEvents")
    private Set<User> userSet = new HashSet<>();
}
