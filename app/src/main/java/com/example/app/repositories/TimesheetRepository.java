package com.example.app.repositories;

import com.example.app.entities.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TimesheetRepository extends JpaRepository<Timesheet, UUID> {

}
