package com.example.app.models.responses.leave;

import com.example.app.entities.LeaveType;
import com.example.app.entities.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class MyLeaveResponse implements LeaveResponseEntity{
    private UUID leaveId;
    private LeaveType leaveType;
    private LocalDate leaveStarts;
    private LocalDate leaveEnds;
    private UUID approvedBy;
    private LocalDate approvedOn;
    private Boolean approved;
    public Boolean isApproved(){
        return this.approved;
    }
}
