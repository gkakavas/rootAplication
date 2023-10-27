package com.example.app.controllers;

import com.example.app.exception.UserNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RequestMapping("/Default")
public interface CrudController <RESPONSE_ENTITY ,REQUEST_ENTITY, EXCEPTION extends Throwable>{
    @PostMapping("/create")
    ResponseEntity<RESPONSE_ENTITY> create (@RequestBody REQUEST_ENTITY request) throws UserNotFoundException;
    @GetMapping("/{id}")
    ResponseEntity<RESPONSE_ENTITY> readOne (@PathVariable UUID id) throws EXCEPTION;
    @GetMapping ("/all")
    ResponseEntity<List<RESPONSE_ENTITY>> readAll();
    @PutMapping("/update/{id}")
    ResponseEntity<RESPONSE_ENTITY> update(@PathVariable UUID id, @RequestBody REQUEST_ENTITY entity) throws EXCEPTION;
    @DeleteMapping("/delete/{id}")
    ResponseEntity<RESPONSE_ENTITY> delete(@PathVariable UUID id) throws EXCEPTION;
}
