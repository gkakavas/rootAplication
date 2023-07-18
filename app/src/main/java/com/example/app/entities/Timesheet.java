package com.example.app.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name= "Timesheet")
@Table(name= "_timesheet")
public class Timesheet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID timesheetId;
    private LocalDate uploadedOn;
    private String accessUrl;
    @OneToOne(mappedBy = "timesheet")
    private User uploadedBy;
}
