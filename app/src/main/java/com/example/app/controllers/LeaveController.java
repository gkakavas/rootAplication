package com.example.app.controllers;

import com.example.app.exception.LeaveNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.LeaveRequestEntity;
import com.example.app.models.responses.leave.LeaveResponseEntity;
import com.example.app.services.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.app.models.requests.RequestId;

@RestController
@RequestMapping("/leave")
@RequiredArgsConstructor
public class LeaveController implements CrudController<LeaveResponseEntity, LeaveRequestEntity, LeaveNotFoundException> {

    private final LeaveService service;

    @Override
    public ResponseEntity<LeaveResponseEntity> create(@Valid LeaveRequestEntity request, String token) throws UserNotFoundException {
        return new ResponseEntity<>(service.create(request,token), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<LeaveResponseEntity> readOne(RequestId id) throws LeaveNotFoundException {
        return new ResponseEntity<>(service.read(id.getUuid()),HttpStatus.OK);
    }

    @Override
    public List<LeaveResponseEntity> readAll() {
        return service.read();
    }

    @Override
    public ResponseEntity<LeaveResponseEntity> update(RequestId id, @Valid LeaveRequestEntity request)
    throws LeaveNotFoundException{
        return new ResponseEntity<>(service.update(id.getUuid(),request), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<LeaveResponseEntity> delete(RequestId id)
    throws LeaveNotFoundException{
        service.delete(id.getUuid());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/approval/{id}")
    public ResponseEntity<LeaveResponseEntity> approveLeave(
            @PathVariable RequestId id)
    throws LeaveNotFoundException,UserNotFoundException{
        return new ResponseEntity<>(service.approveLeave(id.getUuid()),HttpStatus.OK);
    }
}
