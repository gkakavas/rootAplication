package com.example.app.utils.leave;

import com.example.app.entities.Leave;
import com.example.app.entities.User;
import com.example.app.models.requests.LeaveRequestEntity;
import com.example.app.models.responses.leave.AdminHrMngLeaveResponse;
import com.example.app.models.responses.leave.LeaveResponseEntity;
import com.example.app.models.responses.leave.MyLeaveResponse;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.user.EntityResponseUserConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EntityResponseLeaveConverterImpl implements EntityResponseLeaveConverter {
    private final EntityResponseUserConverter userConverter;
    private final UserRepository userRepo;
    @Override
    public LeaveResponseEntity fromLeaveToAdminHrMngLeave(Leave leave) {
        var response= AdminHrMngLeaveResponse.builder()
                .leaveId(leave.getLeaveId())
                .leaveType(leave.getLeaveType())
                .leaveStarts(leave.getLeaveStarts())
                .leaveEnds(leave.getLeaveEnds())
                .approvedBy(null)
                .approvedOn(leave.getApprovedOn())
                .approved(leave.isApproved())
                .requestedBy(userConverter.fromUserToAdminUser(leave.getRequestedBy()))
                .build();
        try{
            response.setApprovedBy(userRepo.findById(leave.getApprovedBy()).orElseThrow().getEmail());
        }catch (NoSuchElementException e){
            response.setApprovedBy(null);
        }
        return response;
    }
    @Override
    public LeaveResponseEntity fromLeaveToMyLeave(Leave leave) {
        var response =  MyLeaveResponse.builder()
                .leaveId(leave.getLeaveId())
                .leaveType(leave.getLeaveType())
                .leaveStarts(leave.getLeaveStarts())
                .leaveEnds(leave.getLeaveEnds())
                .approved(leave.isApproved())
                .approvedBy(null)
                .approvedOn(leave.getApprovedOn())
                .build();
        try{
            response.setApprovedBy(userRepo.findById(leave.getApprovedBy()).orElseThrow().getEmail());
        }catch (NoSuchElementException e){
            response.setApprovedBy(null);
        }
        return response;
    }

    @Override
    public List<LeaveResponseEntity> fromLeaveListToAdminHrMngLeaveList(Set<Leave> leaveList) {
        List<LeaveResponseEntity> responseList = new ArrayList<>();
        leaveList.forEach((leave)->responseList.add(fromLeaveToAdminHrMngLeave(leave)));
        return responseList;
    }

    @Override
    public List<LeaveResponseEntity> fromLeaveListToMyLeaveList(Set<Leave> leaveList) {
        List<LeaveResponseEntity> responseList = new ArrayList<>();
        leaveList.forEach((leave)->responseList.add(fromLeaveToMyLeave(leave)));
        return responseList;
    }

    @Override
        public Leave fromRequestToEntity(LeaveRequestEntity request, User requestedBy) {
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

    @Override
    public Leave fromRequestToEntity(LeaveRequestEntity request, Leave leave) {
        leave.setLeaveType(request.getLeaveType());
        leave.setLeaveStarts(request.getLeaveStarts());
        leave.setLeaveEnds(request.getLeaveEnds());
        return leave;
    }
}
