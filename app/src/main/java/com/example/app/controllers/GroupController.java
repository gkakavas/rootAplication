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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController implements CrudController<GroupResponseEntity,GroupRequestEntity, GroupNotFoundException>{
    private final GroupService service;

    @Override
    public ResponseEntity<GroupResponseEntity> create(@Valid GroupRequestEntity request, String token)
    throws UserNotFoundException {
        return new ResponseEntity<>(service.create(request,token),HttpStatus.CREATED);
    }
    @Override
    public ResponseEntity<GroupResponseEntity> readOne(UUID id)
    throws GroupNotFoundException{
        return new ResponseEntity<>(service.read(id),HttpStatus.OK);
    }
    @Override
    public List<GroupResponseEntity> readAll() {
        return service.read();
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

//    these methods will be accessible in ROLE_ADMIN or ROLE_USER
//    @PatchMapping("{groupId}/addUser?userId=...")
//    @PatchMapping("{groupId}/removeUser?userId=...")
}
