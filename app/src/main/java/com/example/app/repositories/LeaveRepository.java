package com.example.app.repositories;

import com.example.app.entities.Group;
import com.example.app.entities.Leave;
import com.example.app.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeaveRepository extends JpaRepository<Leave, UUID> {
    List<Leave> findAllByRequestedBy(User user);
    List<Leave> findAllByRequestedBy_Group(Group group);
}
