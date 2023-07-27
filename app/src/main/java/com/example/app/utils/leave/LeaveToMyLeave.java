package com.example.app.utils.leave;

import com.example.app.entities.Leave;
import com.example.app.models.responses.leave.LeaveResponseEntity;
import com.example.app.models.responses.leave.MyLeaveResponse;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class LeaveToMyLeave {
    public LeaveResponseEntity convertToMyLeave(Leave leave){
        return MyLeaveResponse.builder()
                .leaveId(leave.getLeaveId())
                .leaveType(leave.getLeaveType())
                .leaveStarts(leave.getLeaveStarts())
                .leaveEnds(leave.getLeaveEnds())
                .approved(leave.isApproved())
                .build();
    }
}
