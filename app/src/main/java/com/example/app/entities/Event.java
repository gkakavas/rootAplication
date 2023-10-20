package com.example.app.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString(exclude = "usersJoinInEvent")
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
    @Lob
    private String eventBody;
    private UUID eventCreator;
    private LocalDateTime eventDateTime;
    private LocalDateTime eventExpiration;

    @JsonIgnore
    @ManyToMany(mappedBy = "userHasEvents", cascade ={CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<User> usersJoinInEvent = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        return new EqualsBuilder().append(eventId, event.eventId).append(eventDescription, event.eventDescription).append(eventBody, event.eventBody).append(eventCreator, event.eventCreator).append(eventDateTime, event.eventDateTime).append(eventExpiration, event.eventExpiration).append(usersJoinInEvent, event.usersJoinInEvent).isEquals();
    }
}
