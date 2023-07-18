package com.example.app.models.responses;

import com.example.app.entities.User;
import jakarta.persistence.OneToOne;
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
public class TimesheetResponseEntity {
    private UUID timesheetId;
    private LocalDate uploadedOn;
    private String accessUrl;
    @OneToOne(mappedBy = "timesheet")
    private User uploadedBy;
}
