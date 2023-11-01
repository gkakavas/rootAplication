package com.example.app.controllers;

import com.example.app.exception.GroupNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.group.GroupResponseEntity;
import com.example.app.services.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController implements CrudController<GroupResponseEntity,GroupRequestEntity, GroupNotFoundException>{
    private final GroupService service;

    @Override
    public ResponseEntity<GroupResponseEntity> create(@Valid GroupRequestEntity request, Principal connectedUser)
    throws UserNotFoundException {
        return new ResponseEntity<>(service.create(request,connectedUser),HttpStatus.CREATED);
    }
    @Override
    public ResponseEntity<GroupResponseEntity> readOne(UUID id,Principal connectedUser)
    throws GroupNotFoundException{
        return new ResponseEntity<>(service.read(id,connectedUser),HttpStatus.OK);
    }
    @Override
    public ResponseEntity<List<GroupResponseEntity>> readAll(Principal connectedUser) {
       return new ResponseEntity<>((service.read(connectedUser)),HttpStatus.OK);
    }
    @Override
    public ResponseEntity<GroupResponseEntity> update(UUID id, GroupRequestEntity request)
    throws GroupNotFoundException{
        return new ResponseEntity<>(service.update(id,request),HttpStatus.OK);
    }
    @Override
    public ResponseEntity<GroupResponseEntity> delete(UUID id)
    throws GroupNotFoundException{
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
