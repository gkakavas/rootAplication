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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LeaveService implements CrudService<LeaveResponseEntity, LeaveRequestEntity, LeaveNotFoundException> {
    private final LeaveRepository leaveRepo;
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final EntityResponseLeaveConverter leaveConverter;
    private final EntityResponseCommonConverter commonConverter;
    @Override
    public LeaveResponseEntity create(LeaveRequestEntity request, String token) throws UserNotFoundException{
            var user = userRepo.findByEmail(jwtService.extractUsername(token.substring(7))).orElseThrow(UserNotFoundException::new);
            var newLeave = leaveRepo.save(leaveConverter.fromRequestToEntity(request,user));
            return leaveConverter.fromLeaveToMyLeave(newLeave);
    }
    @Override
    public LeaveResponseEntity read(UUID id) throws LeaveNotFoundException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser =userRepo.findByEmail(userDetails.getUsername()).orElseThrow(()
                -> new AccessDeniedException("You have not authority to access this resource"));
        Leave leave = leaveRepo.findById(id).orElseThrow(LeaveNotFoundException::new);
        if(currentUser.getRole().equals(Role.ADMIN)||currentUser.getRole().equals(Role.HR)){
            return leaveConverter.fromLeaveToAdminHrMngLeave(leave);
        }
        else if(currentUser.getRole().equals(Role.MANAGER)&&currentUser.getGroup().equals(leave.getRequestedBy().getGroup())){
            return leaveConverter.fromLeaveToAdminHrMngLeave(leave);
        }
        else if(currentUser.getRole().equals(Role.USER)&&leave.getRequestedBy().equals(currentUser)){
            return leaveConverter.fromLeaveToMyLeave(leave);
        }
        else throw new AccessDeniedException("You have not authority to access this resource");
    }

    @Override
    public List<LeaveResponseEntity> read() {
        var currentUser = userRepo.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()
                -> new AccessDeniedException("You have not authority to access this resource"));

        if (currentUser.getRole().equals(Role.HR)||currentUser.getRole().equals(Role.ADMIN)) {
            var users = Set.copyOf(userRepo.findAll());
            return List.copyOf(commonConverter.usersWithLeaves((users)));
        }
        else if (currentUser.getRole().equals(Role.MANAGER) && currentUser.getGroup()!=null) {
            var users = Set.copyOf(userRepo.findAllByGroup(currentUser.getGroup()));
            return List.copyOf(commonConverter.usersWithLeaves(users));
        }
        else if(currentUser.getRole().equals(Role.USER)) {
            return leaveConverter.fromLeaveListToMyLeaveList(currentUser.getUserRequestedLeaves());
        }
        else throw new AccessDeniedException("You have not authority to access this resource");
    }

    @Override
    public LeaveResponseEntity update(UUID id, LeaveRequestEntity request) throws LeaveNotFoundException {
        var currentUser = userRepo.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()
                -> new AccessDeniedException("You have not authority to access this resource"));
        if(currentUser.getRole().equals(Role.USER)){
            var leave = leaveRepo.findById(id).orElseThrow(LeaveNotFoundException::new);
            var updatedLeave = leaveConverter.fromRequestToEntity(request,leave);
            var newLeave = leaveRepo.save(updatedLeave);
            return leaveConverter.fromLeaveToMyLeave(newLeave);
        }
        return null;
    }

    @Override
    public boolean delete(UUID id) throws LeaveNotFoundException {
        if(leaveRepo.existsById(id)){
            leaveRepo.deleteById(id);
            return true;
        }
        else{
            throw new LeaveNotFoundException();
        }

    }

    public LeaveResponseEntity approveLeave(UUID leaveId,String token) throws LeaveNotFoundException,UserNotFoundException{
        var leave = leaveRepo.findById(leaveId).orElseThrow(LeaveNotFoundException::new);
        if(!leave.isApproved()) {
            leave.setApprovedBy(userRepo.findByEmail(
                            jwtService.extractUsername(token.substring(7))).orElseThrow(
                            UserNotFoundException::new)
                    .getUserId());
            leave.setApprovedOn(LocalDate.now());
            leave.setApproved(true);
            var patcedLeave = leaveRepo.save(leave);
            return leaveConverter.fromLeaveToAdminHrMngLeave(patcedLeave);
        }
        return null;
    }
}
