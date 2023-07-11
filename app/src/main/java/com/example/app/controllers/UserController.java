package com.example.app.controllers;

import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.UserResponseEntity;
import com.example.app.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController

@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController implements CrudController<UserResponseEntity, UserRequestEntity>{
    private final UserService service;
    @Override
    public ResponseEntity<UserResponseEntity> create(@Valid UserRequestEntity request,String header) {
        var response = service.create(request,header);
        if(response!=null) {
            return new ResponseEntity<>(service.create(request, header), HttpStatus.CREATED);
        }
        else
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
    @Override
    public ResponseEntity<UserResponseEntity> readOne(UUID id) {
        var response = service.read(id);
        if(response!=null) {
            return new ResponseEntity<>(service.read(id), HttpStatus.OK);
        }
        else
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

    }

    @Override
    public List<UserResponseEntity> readAll() {
        var response = service.read();
        if(response!=null) {
            return service.read();
        }
        else
            return null;
    }

    @Override
    public ResponseEntity<UserResponseEntity> update(UUID id,@Valid UserRequestEntity request) {
        var response = service.read(id);
        if(response!=null) {
            return new ResponseEntity<>(service.update(id,request), HttpStatus.OK);
        }
        else
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<UserResponseEntity> delete(UUID id) {
        var isRemoved = service.delete(id);

        if (!isRemoved) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else
            return new ResponseEntity<>(HttpStatus.OK);
    }
}

