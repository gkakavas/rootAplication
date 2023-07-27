package com.example.app.utils.leave;

import com.example.app.entities.Leave;
import com.example.app.entities.User;
import com.example.app.models.requests.LeaveRequestEntity;
import org.springframework.stereotype.Component;

@Component
public class LeaveRequestEntityToLeave {
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
}
