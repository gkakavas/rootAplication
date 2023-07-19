package com.example.app.services;

import com.example.app.entities.Leave;
import com.example.app.models.requests.LeaveRequestEntity;
import com.example.app.models.responses.LeaveResponseEntity;
import com.example.app.repositories.LeaveRepository;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.LeaveMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaveService implements CrudService<LeaveResponseEntity, LeaveRequestEntity> {
    private final LeaveRepository leaveRepo;
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final LeaveMapper leaveMapper;

    @Override
    public LeaveResponseEntity create(LeaveRequestEntity request, String token) {
        if(request!=null&&token!=null){
            var user = userRepo.findByEmail(jwtService.extractUsername(token.substring(7))).orElseThrow(()->new IllegalArgumentException("Not found user with this email"));
            var newLeave = leaveRepo.save(leaveMapper.convertToEntity(request,user));
            return leaveMapper.convertToResponse(newLeave);
        }
        return null;
    }
    @Override
    public LeaveResponseEntity read(UUID id) {
        if(id!=null){
            var leave = leaveRepo.findById(id).orElseThrow(()->new IllegalArgumentException("Not found leave with this id"));
            return leaveMapper.convertToResponse(leave);
        }
        return null;
    }

    @Override
    public List<LeaveResponseEntity> read() {
        List<Leave> leaveList = leaveRepo.findAll();
        List<LeaveResponseEntity> responseList = new ArrayList<>();
        for(Leave leave: leaveList){
            responseList.add(leaveMapper.convertToResponse(leave));
        }
        return responseList;
    }

    @Override
    public LeaveResponseEntity update(UUID id, LeaveRequestEntity request) {
        if(request!=null&&id!=null){
            var leave = leaveRepo.findById(id).orElseThrow(()->new IllegalArgumentException("Not found leave with this id"));
            var updatedLeave = leaveMapper.convertToEntity(request,leave.getRequestedBy());
            updatedLeave.setLeaveId(id);
            var newLeave = leaveRepo.save(updatedLeave);
            return leaveMapper.convertToResponse(newLeave);
        }
        return null;
    }

    @Override
    public boolean delete(UUID id) {
        if(leaveRepo.existsById(id)){
            leaveRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public LeaveResponseEntity approveLeave(UUID leaveId,String token){
        var leave = leaveRepo.findById(leaveId).orElseThrow(()-> new IllegalArgumentException("Not found leave with this id"));
        if(!leave.isApproved()) {
            leave.setApprovedBy(userRepo.findByEmail(
                            jwtService.extractUsername(token.substring(7))).orElseThrow(
                            () -> new IllegalArgumentException("Not found user with this email"))
                    .getUserId());
            leave.setApprovedOn(LocalDate.now());
            leave.setApproved(true);
            var patcedLeave = leaveRepo.save(leave);
            return leaveMapper.convertToResponse(patcedLeave);
        }
        return null;
    }
}
