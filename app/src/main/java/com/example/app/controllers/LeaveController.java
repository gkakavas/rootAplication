package com.example.app.controllers;

import com.example.app.entities.User;
import com.example.app.exception.LeaveNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.LeaveRequestEntity;
import com.example.app.models.responses.leave.LeaveResponseEntity;
import com.example.app.services.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService service;

    @PostMapping("/create")
    public ResponseEntity<LeaveResponseEntity> create(
            @Validated LeaveRequestEntity request,
            @AuthenticationPrincipal User connectedUser) throws UserNotFoundException {
        return new ResponseEntity<>(service.create(request,connectedUser), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveResponseEntity> readOne(
            @PathVariable UUID id,
            @AuthenticationPrincipal User connectedUser) throws LeaveNotFoundException {
        return new ResponseEntity<>(service.read(id,connectedUser),HttpStatus.OK);
    }

    @GetMapping ("/all")
    public ResponseEntity<List<LeaveResponseEntity>> readAll(@AuthenticationPrincipal User connectedUser) {
        return new ResponseEntity<>(service.read(connectedUser),HttpStatus.OK) ;
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<LeaveResponseEntity> update(
            @PathVariable UUID id,
            @Validated LeaveRequestEntity request,
            @AuthenticationPrincipal User connectedUser)
    throws LeaveNotFoundException{
        return new ResponseEntity<>(service.update(id,request,connectedUser), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<LeaveResponseEntity> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User connectedUser)
    throws LeaveNotFoundException{
        service.delete(id,connectedUser);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/approval/{id}")
    public ResponseEntity<LeaveResponseEntity> approveLeave(
            @PathVariable UUID id,
            @AuthenticationPrincipal User connectedUser)
    throws LeaveNotFoundException,UserNotFoundException{
        return new ResponseEntity<>(service.approveLeave(id,connectedUser),HttpStatus.OK);
    }
}
