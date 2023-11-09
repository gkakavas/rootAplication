package com.example.app.models.responses.leave;

import com.example.app.entities.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminHrMngLeaveResponse implements LeaveResponseEntity {
    private UUID leaveId;
    private LeaveType leaveType;
    private LocalDate leaveStarts;
    private LocalDate leaveEnds;
    private String approvedBy;
    private LocalDateTime approvedOn;
    private Boolean approved;
    private String requestedBy;

    public Boolean isApproved(){
        return this.approved;
    }
}
