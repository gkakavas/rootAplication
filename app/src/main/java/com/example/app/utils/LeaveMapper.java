package com.example.app.utils;

import com.example.app.entities.Leave;
import com.example.app.entities.User;
import com.example.app.models.requests.LeaveRequestEntity;
import com.example.app.models.responses.LeaveResponseEntity;
import org.springframework.stereotype.Component;
@Component
public class LeaveMapper {
    public Leave convertToEntity(LeaveRequestEntity request, User requestedBy) {
        return Leave.builder()
                .leaveType(request.getLeaveType())
                .leaveStarts(request.getLeaveStarts())
                .leaveEnds(request.getLeaveEnds())
                .approvedBy(null)
                .approvedOn(null)
                .approved(false)
                .requestedBy(requestedBy)
                .build();
    }
    public LeaveResponseEntity convertToResponse(Leave leave) {
        return LeaveResponseEntity.builder()
                .leaveId(leave.getLeaveId())
                .leaveType(leave.getLeaveType())
                .leaveStarts(leave.getLeaveStarts())
                .leaveEnds(leave.getLeaveEnds())
                .approvedBy(leave.getApprovedBy())
                .approvedOn(leave.getApprovedOn())
                .approved(leave.isApproved())
                .requestedBy(leave.getRequestedBy())
                .build();
    }
}
