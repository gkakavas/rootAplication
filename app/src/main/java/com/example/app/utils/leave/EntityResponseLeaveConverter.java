package com.example.app.utils.leave;

import com.example.app.entities.Leave;
import com.example.app.entities.User;
import com.example.app.models.requests.LeaveRequestEntity;
import com.example.app.models.responses.leave.LeaveResponseEntity;

import java.util.List;
import java.util.Set;

public interface EntityResponseLeaveConverter {
    LeaveResponseEntity fromLeaveToAdminHrMngLeave(Leave leave);
    LeaveResponseEntity fromLeaveToMyLeave(Leave leave);
    List<LeaveResponseEntity> fromLeaveListToAdminHrMngLeaveList(Set<Leave> leaveList);
    List<LeaveResponseEntity> fromLeaveListToMyLeaveList(Set<Leave> leaveList);
    Leave fromRequestToEntity(LeaveRequestEntity request, User requestedBy);
    Leave fromRequestToEntity(LeaveRequestEntity request, Leave leave);
}
