package com.example.app.services;

import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.LeaveNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.LeaveRequestEntity;
import com.example.app.models.responses.leave.LeaveResponseEntity;
import com.example.app.repositories.LeaveRepository;
import com.example.app.utils.converters.leave.EntityResponseLeaveConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRepository leaveRepo;
    private final EntityResponseLeaveConverter leaveConverter;


    public LeaveResponseEntity create(LeaveRequestEntity request, User connectedUser) {
        var newLeave = leaveRepo.save(leaveConverter.fromRequestToEntity(request, connectedUser));
        return leaveConverter.fromLeaveToMyLeave(newLeave);
    }
    public LeaveResponseEntity read(UUID id, User connectedUser) throws LeaveNotFoundException {
        var leave = leaveRepo.findById(id).orElseThrow(LeaveNotFoundException::new);
        if(List.of(Role.ADMIN,Role.HR).contains(connectedUser.getRole())){
            return leaveConverter.fromLeaveToAdminHrMngLeave(leave);
        }
        else if(connectedUser.getRole().equals(Role.MANAGER) && connectedUser.getGroup().equals(leave.getRequestedBy().getGroup())){
            return leaveConverter.fromLeaveToAdminHrMngLeave(leave);
        }
        else if(connectedUser.getRole().equals(Role.USER) && leave.getRequestedBy().equals(connectedUser)){
            return leaveConverter.fromLeaveToMyLeave(leave);
        }
        else throw new AccessDeniedException("You have not authority to access this resource");
    }


    public List<LeaveResponseEntity> read(User connectedUser) {
        if (connectedUser.getRole().equals(Role.ADMIN)||connectedUser.getRole().equals(Role.HR)) {
            var leaves = leaveRepo.findAll();
            return List.copyOf(leaveConverter.fromLeaveListToAdminHrMngLeaveList(Set.copyOf(leaves)));
        }
        else if (connectedUser.getRole().equals(Role.MANAGER) && connectedUser.getGroup()!=null) {
            var leaves = leaveRepo.findAllByRequestedBy_Group(connectedUser.getGroup());
            return List.copyOf(leaveConverter.fromLeaveListToAdminHrMngLeaveList(Set.copyOf(leaves)));
        }
        else{
            return leaveConverter.fromLeaveListToMyLeaveList(connectedUser.getUserRequestedLeaves());
        }
    }


    public LeaveResponseEntity update(UUID id, LeaveRequestEntity request, User connectedUser) throws LeaveNotFoundException {
        var leave = leaveRepo.findById(id).orElseThrow(LeaveNotFoundException::new);
        if(connectedUser.getRole().equals(Role.ADMIN)
        ||(connectedUser.getRole().equals(Role.USER)
           && connectedUser.getUserRequestedLeaves().stream().anyMatch(leave1 -> leave1.equals(leave)))) {

            var updatedLeave = leaveConverter.updateLeave(request, leave);
            var newLeave = leaveRepo.save(updatedLeave);
            return leaveConverter.fromLeaveToMyLeave(newLeave);
        }
        else throw new AccessDeniedException("You have not authority to update this resource");
    }


    public boolean delete(UUID id, User connectedUser) throws LeaveNotFoundException {
        var leave =  leaveRepo.findById(id).orElseThrow(LeaveNotFoundException::new);
        if(connectedUser.getRole().equals(Role.ADMIN)
          ||(connectedUser.getRole().equals(Role.USER)
          && connectedUser.getUserRequestedLeaves().stream().anyMatch(leave1 -> leave1.equals(leave)))){
            leave.getRequestedBy().getUserRequestedLeaves().remove(leave);
            leaveRepo.delete(leave);
            return !leaveRepo.existsById(id);
        }
        else throw new AccessDeniedException("You have not authority to delete this resource");
    }

    public LeaveResponseEntity approveLeave(UUID leaveId,User connectedUser) throws LeaveNotFoundException,UserNotFoundException{
        var leave = leaveRepo.findById(leaveId).orElseThrow(LeaveNotFoundException::new);
        if(!leave.isApproved()) {
            var patcedLeave = leaveRepo.save(leaveConverter.approveLeave(leave,connectedUser));
            return leaveConverter.fromLeaveToAdminHrMngLeave(patcedLeave);
        }
        else return leaveConverter.fromLeaveToAdminHrMngLeave(leave);//to implemented custom exception LeaveFailedToBeApprovedException
    }

}
