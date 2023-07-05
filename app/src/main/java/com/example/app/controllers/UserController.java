package com.example.app.controllers;

import com.example.app.models.*;
import com.example.app.services.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController implements CrudController<UserResponseEntity, UserRequestEntity>{
    private final UserService service;
    @Override
    public ResponseEntity<UserResponseEntity> create(@NotNull UserRequestEntity userRequestEntity) {
        return ResponseEntity.ok(service.create(userRequestEntity));
    }
    @Override
    public ResponseEntity<UserResponseEntity> readOne(@NotNull UUID id) {
        return ResponseEntity.ok(service.read(id));
    }

    @Override
    public List<UserResponseEntity> readAll() {
        return service.read();
    }

    @Override
    public ResponseEntity<UserResponseEntity> update(@NotNull UUID id, @NotNull UserRequestEntity userRequestEntity) {
        return ResponseEntity.ok(service.update(id, userRequestEntity));
    }

    @Override
    public ResponseEntity<UserResponseEntity> delete(@NotNull UUID id) {
        var isRemoved = service.delete(id);

        if (!isRemoved) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}

