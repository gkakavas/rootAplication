package com.example.app.controllers;

import com.example.app.models.*;
import com.example.app.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController

@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController implements CrudController<UserResponseEntity, UserRequestEntity>{
    private final UserService service;
    @Override
    public ResponseEntity<UserResponseEntity> create(UserRequestEntity userRequestEntity) {
        return ResponseEntity.ok(service.create(userRequestEntity));
    }
    @Override
    public ResponseEntity<UserResponseEntity> readOne(UUID id) {
        return ResponseEntity.ok(service.read(id));
    }

    @Override
    public List<UserResponseEntity> readAll() {
        return service.read();
    }

    @Override
    public ResponseEntity<UserResponseEntity> update(UUID id,UserRequestEntity userRequestEntity) {
        return ResponseEntity.ok(service.update(id, userRequestEntity));
    }

    @Override
    public ResponseEntity<UserResponseEntity> delete(UUID id) {
        var isRemoved = service.delete(id);

        if (!isRemoved) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

//    @PostMapping("/event/{eventId}")
//    public ResponseEntity<List<UserResponseEntity>> addUserToEvent(
//            @PathVariable UUID eventId, @RequestBody List<UUID> userIds){
//        return ResponseEntity.ok(service.addEventsToUser(eventId, userIds));
//    }
}

