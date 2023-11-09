package com.example.app.controllers;

import com.example.app.entities.User;
import com.example.app.exception.GroupNotFoundException;
import com.example.app.exception.NewPasswordConfirmationNewPasswordNotMatchException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.exception.WrongOldPasswordProvidedException;
import com.example.app.models.requests.ChangePasswordRequest;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.event.EventResponseEntity;
import com.example.app.models.responses.user.UserResponseEntity;
import com.example.app.services.UserService;
import com.example.app.utils.validators.user.UserPatchValue;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
public class UserController implements CrudController<UserResponseEntity, UserRequestEntity, UserNotFoundException>{
    private final UserService service;

    @Override
    public ResponseEntity<UserResponseEntity> create
            (@Validated UserRequestEntity request,
             @AuthenticationPrincipal User connectedUser
            ) throws UserNotFoundException, GroupNotFoundException {
        return new ResponseEntity<>(service.create(request,connectedUser), HttpStatus.CREATED);
    }
    @Override
    public ResponseEntity<UserResponseEntity> readOne(UUID id,@AuthenticationPrincipal User connectedUser) throws UserNotFoundException {
        return new ResponseEntity<>(service.read(id,connectedUser), HttpStatus.OK);
    }
    @Override
    public ResponseEntity<List<UserResponseEntity>> readAll(@AuthenticationPrincipal User connectedUser) {
        return new ResponseEntity<>(service.read(connectedUser),HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserResponseEntity> update(UUID id, @Validated UserRequestEntity request) throws UserNotFoundException{
        return new ResponseEntity<>(service.update(id,request), HttpStatus.OK);
    }
    @Override
    public ResponseEntity<UserResponseEntity> delete(UUID id) throws UserNotFoundException {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @PatchMapping("/patch/{id}")
    public ResponseEntity<UserResponseEntity> patch
            (@PathVariable UUID id,
             @RequestBody @UserPatchValue Map<String, String> request)
            throws UserNotFoundException, GroupNotFoundException {
        return new ResponseEntity<>(service.patch(id,request),HttpStatus.OK);
    }
    @PreAuthorize("#id == authentication.principal.userId")
    @GetMapping("/{id}/events")
    public ResponseEntity<Set<EventResponseEntity>> readUserEvents(@PathVariable UUID id) throws UserNotFoundException {
        return new ResponseEntity<>(service.readUserEvents(id),HttpStatus.OK);
    }

    @PatchMapping("/changePassword")
    public ResponseEntity<UserResponseEntity> changePassword(
            @RequestBody @Validated ChangePasswordRequest request,
            @AuthenticationPrincipal User connectedUser) throws NewPasswordConfirmationNewPasswordNotMatchException, WrongOldPasswordProvidedException {
        return new ResponseEntity<>(service.changePassword(request,connectedUser),HttpStatus.OK);
    }
}

