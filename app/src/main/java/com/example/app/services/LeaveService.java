package com.example.app.services;

import com.example.app.entities.Leave;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.GroupNotFoundException;
import com.example.app.exception.LeaveNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.LeaveRequestEntity;
import com.example.app.models.responses.leave.LeaveResponseEntity;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.LeaveRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.leave.*;
import com.example.app.utils.user.UserListWithLeavesConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LeaveService implements CrudService<LeaveResponseEntity, LeaveRequestEntity, LeaveNotFoundException> {
    private final LeaveRepository leaveRepo;
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final LeaveRequestEntityToLeave toLeave;
    private final LeaveToMyLeave toMyLeave;
    private final LeaveToAdminHrMngLeave toAdminHrMngLeave;
    private final LeaveListToMyLeaveList toMyLeaveList;
    private final UserListWithLeavesConverter userListWithLeavesConverter;
    @Override
    public LeaveResponseEntity create(LeaveRequestEntity request, String token) throws UserNotFoundException{
        if(request!=null&&token!=null){
            var user = userRepo.findByEmail(jwtService.extractUsername(token.substring(7))).orElseThrow(UserNotFoundException::new);
            var newLeave = leaveRepo.save(toLeave.convertToEntity(request,user));
            return toMyLeave.convertToMyLeave(newLeave);
        }
        return null;
    }
    @Override
    public LeaveResponseEntity read(UUID id) throws LeaveNotFoundException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userRepo.findByEmail(userDetails.getUsername()).orElseThrow(()
                -> new AccessDeniedException("You have not authority to access this resource"));
        var leave = leaveRepo.findById(id).orElseThrow(LeaveNotFoundException::new);
        return toAdminHrMngLeave.convertToAdminHrMngLeave(leave);
    }

    @Override
    public List<LeaveResponseEntity> read() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepo.findByEmail(userDetails.getUsername()).orElseThrow(()
                -> new AccessDeniedException("You have not authority to access this resource"));
        if (currentUser.getRole().equals(Role.ROLE_HR)||currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            List<User> userList = userRepo.findAll();
            return userListWithLeavesConverter.userWithLeavesConvert(userList);
        }
        else if (currentUser.getRole().equals(Role.ROLE_MANAGER)) {
            List<User> userList = userRepo.findAllByGroup(currentUser.getGroup());
            return userListWithLeavesConverter.userWithLeavesConvert(userList);
            }
        else {
                List<Leave> myLeaveList = leaveRepo.findAllByRequestedBy(currentUser);
                return toMyLeaveList.convertToMyLeaveList(myLeaveList);
            }
        }

    @Override
    public LeaveResponseEntity update(UUID id, LeaveRequestEntity request) throws LeaveNotFoundException {
        if(request!=null&&id!=null){
            var leave = leaveRepo.findById(id).orElseThrow(LeaveNotFoundException::new);
            var updatedLeave = toLeave.convertToEntity(request,leave.getRequestedBy());
            updatedLeave.setLeaveId(id);
            var newLeave = leaveRepo.save(updatedLeave);
            return toMyLeave.convertToMyLeave(newLeave);
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
            return toAdminHrMngLeave.convertToAdminHrMngLeave(patcedLeave);
        }
        return null;
    }
}
