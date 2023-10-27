package com.example.app.controllers;

import com.example.app.exception.LeaveNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.LeaveRequestEntity;
import com.example.app.models.responses.leave.LeaveResponseEntity;
import com.example.app.services.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/leave")
@RequiredArgsConstructor
public class LeaveController implements CrudController<LeaveResponseEntity, LeaveRequestEntity, LeaveNotFoundException> {

    private final LeaveService service;

    @Override
    public ResponseEntity<LeaveResponseEntity> create(@Valid LeaveRequestEntity request) throws UserNotFoundException {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<LeaveResponseEntity> readOne(UUID id) throws LeaveNotFoundException {
        return new ResponseEntity<>(service.read(id),HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<LeaveResponseEntity>> readAll() {
        return new ResponseEntity<>(service.read(),HttpStatus.OK) ;
    }

    @Override
    public ResponseEntity<LeaveResponseEntity> update(UUID id, @Valid LeaveRequestEntity request)
    throws LeaveNotFoundException{
        return new ResponseEntity<>(service.update(id,request), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<LeaveResponseEntity> delete(UUID id)
    throws LeaveNotFoundException{
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/approval/{id}")
    public ResponseEntity<LeaveResponseEntity> approveLeave(
            @PathVariable UUID id)
    throws LeaveNotFoundException,UserNotFoundException{
        return new ResponseEntity<>(service.approveLeave(id),HttpStatus.OK);
    }
}
