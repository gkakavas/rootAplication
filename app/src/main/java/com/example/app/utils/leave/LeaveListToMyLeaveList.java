package com.example.app.utils.leave;

import com.example.app.entities.Leave;
import com.example.app.models.responses.leave.LeaveResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LeaveListToMyLeaveList {
    private LeaveToMyLeave toMyLeave;
    public List<LeaveResponseEntity> convertToMyLeaveList(List<Leave> leaveList){
        List<LeaveResponseEntity> responseList = new ArrayList<>();
        leaveList.forEach((leave)->responseList.add(
                toMyLeave.convertToMyLeave(leave)));
        return responseList;
    }
}
