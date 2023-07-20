package com.example.app.services;

import com.example.app.entities.Leave;
import com.example.app.exception.LeaveNotFoundException;
import com.example.app.exception.UserNotFoundException;
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
public class LeaveService implements CrudService<LeaveResponseEntity, LeaveRequestEntity, LeaveNotFoundException> {
    private final LeaveRepository leaveRepo;
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final LeaveMapper leaveMapper;

    @Override
    public LeaveResponseEntity create(LeaveRequestEntity request, String token) throws UserNotFoundException{
        if(request!=null&&token!=null){
            var user = userRepo.findByEmail(jwtService.extractUsername(token.substring(7))).orElseThrow(UserNotFoundException::new);
            var newLeave = leaveRepo.save(leaveMapper.convertToEntity(request,user));
            return leaveMapper.convertToResponse(newLeave);
        }
        return null;
    }
    @Override
    public LeaveResponseEntity read(UUID id) throws LeaveNotFoundException {
        if(id!=null){
            var leave = leaveRepo.findById(id).orElseThrow(LeaveNotFoundException::new);
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
    public LeaveResponseEntity update(UUID id, LeaveRequestEntity request) throws LeaveNotFoundException {
        if(request!=null&&id!=null){
            var leave = leaveRepo.findById(id).orElseThrow(LeaveNotFoundException::new);
            var updatedLeave = leaveMapper.convertToEntity(request,leave.getRequestedBy());
            updatedLeave.setLeaveId(id);
            var newLeave = leaveRepo.save(updatedLeave);
            return leaveMapper.convertToResponse(newLeave);
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
            return leaveMapper.convertToResponse(patcedLeave);
        }
        return null;
    }
}
