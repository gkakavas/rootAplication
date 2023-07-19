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
import java.util.UUID;

@RestController
@RequestMapping("/leave")
@RequiredArgsConstructor
public class LeaveController implements CrudController<LeaveResponseEntity, LeaveRequestEntity> {
    private final LeaveService service;
    @Override
    public ResponseEntity<LeaveResponseEntity> create(@Valid LeaveRequestEntity request, String token) {
        return new ResponseEntity<>(service.create(request,token), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<LeaveResponseEntity> readOne(UUID id) {
        return new ResponseEntity<>(service.read(id),HttpStatus.OK);
    }

    @Override
    public List<LeaveResponseEntity> readAll() {
        return service.read();
    }

    @Override
    public ResponseEntity<LeaveResponseEntity> update(UUID id, @Valid LeaveRequestEntity request) {
        return new ResponseEntity<>(service.update(id,request), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<LeaveResponseEntity> delete(UUID id) {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PatchMapping("/approval/{id}")
    public ResponseEntity<LeaveResponseEntity> approveLeave(
            @PathVariable UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token){
        return new ResponseEntity<>(service.approveLeave(id,token),HttpStatus.ACCEPTED);
    }
}
