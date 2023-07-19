package com.example.app.models.responses;

import com.example.app.entities.LeaveType;
import com.example.app.entities.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveResponseEntity {
    private UUID leaveId;
    private LeaveType leaveType;
    private LocalDate leaveStarts;
    private LocalDate leaveEnds;
    private UUID approvedBy;
    private LocalDate approvedOn;
    private Boolean approved;
    private User requestedBy;
}
