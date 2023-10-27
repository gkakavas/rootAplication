package com.example.app.repositories;

import com.example.app.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    Optional<Event> findByEventDescription(String eventDescription);
}
