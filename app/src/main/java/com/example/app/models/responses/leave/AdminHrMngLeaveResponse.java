package com.example.app.models.responses.leave;

import com.example.app.entities.LeaveType;
import com.example.app.entities.User;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
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
