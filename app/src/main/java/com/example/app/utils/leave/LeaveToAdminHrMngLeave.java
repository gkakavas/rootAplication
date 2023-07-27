package com.example.app.utils.leave;

import com.example.app.entities.Leave;
import com.example.app.models.responses.leave.AdminHrMngLeaveResponse;
import com.example.app.models.responses.leave.LeaveResponseEntity;
import com.example.app.models.responses.user.OtherUserResponse;
import com.example.app.utils.user.UserToOtherUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeaveToAdminHrMngLeave {

    private final UserToOtherUser toOtherUser;
    public LeaveResponseEntity convertToAdminHrMngLeave(Leave leave) {
        return AdminHrMngLeaveResponse.builder()
                .leaveId(leave.getLeaveId())
                .leaveType(leave.getLeaveType())
                .leaveStarts(leave.getLeaveStarts())
                .leaveEnds(leave.getLeaveEnds())
                .approvedBy(leave.getApprovedBy())
                .approvedOn(leave.getApprovedOn())
                .approved(leave.isApproved())
                .requestedBy((OtherUserResponse) toOtherUser.convertToOtherUser(leave.getRequestedBy()))
                .build();

    }
}
