package com.example.app.controllers;

import com.example.app.models.requests.LeaveRequestEntity;
import com.example.app.models.responses.LeaveResponseEntity;
import com.example.app.services.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/leave")
@RequiredArgsConstructor
public class LeaveController implements CrudController<LeaveResponseEntity, LeaveRequestEntity> {
    private final LeaveService leaveService;
    @Override
    public ResponseEntity<LeaveResponseEntity> create(@Valid LeaveRequestEntity request, String header) {
        var response = leaveService.create(request,header);
        if(response!=null){
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
    }

    @Override
    public ResponseEntity<LeaveResponseEntity> readOne(UUID id) {
       var response = leaveService.read(id);
       if(response!=null){
           return new ResponseEntity<>(response,HttpStatus.OK);
       }
       return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
    }

    @Override
    public List<LeaveResponseEntity> readAll() {
        return leaveService.read();
    }

    @Override
    public ResponseEntity<LeaveResponseEntity> update(UUID id, @Valid LeaveRequestEntity request) {
        var response = leaveService.update(id,request);
        if(response!=null){
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
    }

    @Override
    public ResponseEntity<LeaveResponseEntity> delete(UUID id) {
        var isRemoved = leaveService.delete(id);
        if (isRemoved) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PatchMapping("/approval/{id}")
    public ResponseEntity<LeaveResponseEntity> approveLeave(@PathVariable UUID id,
                                             @RequestHeader(HttpHeaders.AUTHORIZATION) String token){
        var response = leaveService.approveLeave(id,token);
        if(response!=null){
            return new ResponseEntity<>(response,HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
    }
}
