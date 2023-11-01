package com.example.app.services;

import com.example.app.entities.Leave;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.LeaveNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.LeaveRequestEntity;
import com.example.app.models.responses.leave.LeaveResponseEntity;
import com.example.app.repositories.LeaveRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.common.EntityResponseCommonConverter;
import com.example.app.utils.leave.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRepository leaveRepo;
    private final UserRepository userRepo;
    private final EntityResponseLeaveConverter leaveConverter;


    public LeaveResponseEntity create(LeaveRequestEntity request, Principal connectedUser) {
        var currentUser = (User)((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        var newLeave = leaveRepo.save(leaveConverter.fromRequestToEntity(request, currentUser));
        return leaveConverter.fromLeaveToMyLeave(newLeave);
    }
    public LeaveResponseEntity read(UUID id, Principal connectedUser) throws LeaveNotFoundException {
        var currentUser = (User)((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        var leave = leaveRepo.findById(id).orElseThrow(LeaveNotFoundException::new);
        if(currentUser.getRole().equals(Role.ADMIN)||currentUser.getRole().equals(Role.HR)){
            return leaveConverter.fromLeaveToAdminHrMngLeave(leave);
        }
        else if(currentUser.getRole().equals(Role.MANAGER) && currentUser.getGroup().equals(leave.getRequestedBy().getGroup())){
            return leaveConverter.fromLeaveToAdminHrMngLeave(leave);
        }
        else if(currentUser.getRole().equals(Role.USER)&&leave.getRequestedBy().equals(currentUser)){
            return leaveConverter.fromLeaveToMyLeave(leave);
        }
        else throw new AccessDeniedException("You have not authority to access this resource");
    }


    public List<LeaveResponseEntity> read(Principal connectedUser) {
        var currentUser = (User)((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if (currentUser.getRole().equals(Role.ADMIN)||currentUser.getRole().equals(Role.HR)) {
            var leaves = leaveRepo.findAll();
            return List.copyOf(leaveConverter.fromLeaveListToAdminHrMngLeaveList(Set.copyOf(leaves)));
        }
        else if (currentUser.getRole().equals(Role.MANAGER) && currentUser.getGroup()!=null) {
            var leaves = leaveRepo.findAllByRequestedBy_Group(currentUser.getGroup());
            return List.copyOf(leaveConverter.fromLeaveListToAdminHrMngLeaveList(Set.copyOf(leaves)));
        }
        else{
            return leaveConverter.fromLeaveListToMyLeaveList(currentUser.getUserRequestedLeaves());
        }
    }


    public LeaveResponseEntity update(UUID id, LeaveRequestEntity request) throws LeaveNotFoundException {
        var leave = leaveRepo.findById(id).orElseThrow(LeaveNotFoundException::new);
        var updatedLeave = leaveConverter.updateLeave(request,leave);
        var newLeave = leaveRepo.save(updatedLeave);
        return leaveConverter.fromLeaveToMyLeave(newLeave);
    }


    public boolean delete(UUID id) throws LeaveNotFoundException {
        if(leaveRepo.existsById(id)){
            leaveRepo.deleteById(id);
            return true;
        }
        else{
            throw new LeaveNotFoundException();
        }

    }

    public LeaveResponseEntity approveLeave(UUID leaveId,Principal connectedUser) throws LeaveNotFoundException,UserNotFoundException{
        var leave = leaveRepo.findById(leaveId).orElseThrow(LeaveNotFoundException::new);
        var currentUser = (User)((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if(!leave.isApproved()) {
            var patcedLeave = leaveRepo.save(leaveConverter.approveLeave(leave,currentUser));
            return leaveConverter.fromLeaveToAdminHrMngLeave(patcedLeave);
        }
        else return leaveConverter.fromLeaveToAdminHrMngLeave(leave);
    }

}
