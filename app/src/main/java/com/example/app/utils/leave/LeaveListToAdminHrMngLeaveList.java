package com.example.app.utils.leave;

import com.example.app.entities.Leave;
import com.example.app.models.responses.leave.AdminHrMngLeaveResponse;
import com.example.app.models.responses.leave.LeaveResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LeaveListToAdminHrMngLeaveList {
    private final LeaveToAdminHrMngLeave toAdminHrMngLeave;
    public List<LeaveResponseEntity> convertToAdminHrMngLeaveList(List<Leave> leaveList){
        List<LeaveResponseEntity> responseList = new ArrayList<>();
        leaveList.forEach((leave)->responseList.add(toAdminHrMngLeave.convertToAdminHrMngLeave(leave)));
        return responseList;
    }
}
