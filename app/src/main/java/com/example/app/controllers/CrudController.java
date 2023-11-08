package com.example.app.controllers;

import com.example.app.entities.User;
import com.example.app.exception.GroupNotFoundException;
import com.example.app.exception.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@RequestMapping("/Default")
public interface CrudController <RESPONSE_ENTITY ,REQUEST_ENTITY, EXCEPTION extends Throwable>{
    @PostMapping("/create")
    ResponseEntity<RESPONSE_ENTITY> create (@RequestBody REQUEST_ENTITY request,@AuthenticationPrincipal User connectedUser) throws UserNotFoundException, GroupNotFoundException;
    @GetMapping("/{id}")
    ResponseEntity<RESPONSE_ENTITY> readOne (@PathVariable UUID id, @AuthenticationPrincipal User connectedUser) throws EXCEPTION;
    @GetMapping ("/all")
    ResponseEntity<List<RESPONSE_ENTITY>> readAll(@AuthenticationPrincipal User connectedUser);
    @PutMapping("/update/{id}")
    ResponseEntity<RESPONSE_ENTITY> update(@PathVariable UUID id, @RequestBody REQUEST_ENTITY entity) throws EXCEPTION;
    @DeleteMapping("/delete/{id}")
    ResponseEntity<RESPONSE_ENTITY> delete(@PathVariable UUID id) throws EXCEPTION;
}
