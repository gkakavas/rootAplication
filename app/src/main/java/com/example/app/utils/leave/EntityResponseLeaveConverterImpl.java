package com.example.app.utils.leave;

import com.example.app.entities.Leave;
import com.example.app.entities.LeaveType;
import com.example.app.entities.User;
import com.example.app.models.requests.LeaveRequestEntity;
import com.example.app.models.responses.leave.AdminHrMngLeaveResponse;
import com.example.app.models.responses.leave.LeaveResponseEntity;
import com.example.app.models.responses.leave.MyLeaveResponse;
import com.example.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EntityResponseLeaveConverterImpl implements EntityResponseLeaveConverter {
    private final UserRepository userRepo;
    @Override
    public LeaveResponseEntity fromLeaveToAdminHrMngLeave(Leave leave) {

        var response =  AdminHrMngLeaveResponse.builder()
                .leaveId(leave.getLeaveId())
                .leaveType(leave.getLeaveType())
                .leaveStarts(leave.getLeaveStarts())
                .leaveEnds(leave.getLeaveEnds())
                .approvedBy(null)
                .approvedOn(leave.getApprovedOn())
                .approved(leave.isApproved())
                .requestedBy(leave.getRequestedBy().getEmail())
                .build();
        if(leave.getApprovedBy()!=null){
            userRepo.findById(leave.getApprovedBy()).ifPresent(user->response.setApprovedBy(user.getEmail()));
        }
        return response;
    }
    @Override
    public LeaveResponseEntity fromLeaveToMyLeave(Leave leave) {
        var response = MyLeaveResponse.builder()
                .leaveId(leave.getLeaveId())
                .leaveType(leave.getLeaveType())
                .leaveStarts(leave.getLeaveStarts())
                .leaveEnds(leave.getLeaveEnds())
                .approved(leave.isApproved())
                .approvedBy(null)
                .approvedOn(leave.getApprovedOn())
                .build();
        if(leave.getApprovedBy()!=null){
            userRepo.findById(leave.getApprovedBy()).ifPresent(user->response.setApprovedBy(user.getEmail()));
        }
        return response;
    }

    @Override
    public List<LeaveResponseEntity> fromLeaveListToAdminHrMngLeaveList(Set<Leave> leaveList) {
        return leaveList.stream().map(this::fromLeaveToAdminHrMngLeave).toList();
    }

    @Override
    public List<LeaveResponseEntity> fromLeaveListToMyLeaveList(Set<Leave> leaveList) {
        return leaveList.stream().map(this::fromLeaveToMyLeave).toList();
    }

    @Override
        public Leave fromRequestToEntity(LeaveRequestEntity request, User requestedBy) {
        return Leave.builder()
                .leaveType(LeaveType.valueOf(request.getLeaveType()))
                .leaveStarts(LocalDate.parse(request.getLeaveStarts()))
                .leaveEnds(LocalDate.parse(request.getLeaveEnds()))
                .approvedBy(null)
                .approvedOn(null)
                .approved(false)
                .requestedBy(requestedBy)
                .build();
    }

    @Override
    public Leave updateLeave(LeaveRequestEntity request, Leave leave) {
        leave.setLeaveType(LeaveType.valueOf(request.getLeaveType()));
        leave.setLeaveStarts(LocalDate.parse(request.getLeaveStarts()));
        leave.setLeaveEnds(LocalDate.parse(request.getLeaveEnds()));
        return leave;
    }

    @Override
    public Leave approveLeave(Leave leave, User user) {
        leave.setApproved(true);
        leave.setApprovedOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        leave.setApprovedBy(user.getUserId());
        return leave;
    }
}
