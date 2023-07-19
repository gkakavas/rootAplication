package com.example.app.controllers;

import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.GroupResponseEntity;
import com.example.app.services.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController implements CrudController<GroupResponseEntity,GroupRequestEntity>{
    private final GroupService service;
    @Override
    public ResponseEntity<GroupResponseEntity> create(@Valid GroupRequestEntity request, String token) {
        return new ResponseEntity<>(service.create(request,token),HttpStatus.OK);
    }
    @Override
    public ResponseEntity<GroupResponseEntity> readOne(UUID id) {
        return new ResponseEntity<>(service.read(id),HttpStatus.OK);
    }

    @Override
    public List<GroupResponseEntity> readAll() {
        return service.read();
    }

    @Override
    public ResponseEntity<GroupResponseEntity> update(UUID id, GroupRequestEntity request) {
        return new ResponseEntity<>(service.update(id,request),HttpStatus.OK);
    }
    @Override
    public ResponseEntity<GroupResponseEntity> delete(UUID id) {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
