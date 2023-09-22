package com.example.app.controllers;

import com.example.app.exception.UserNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.app.models.requests.RequestId;
@RequestMapping("/Default")
public interface CrudController <RESPONSE_ENTITY ,REQUEST_ENTITY, EXCEPTION extends Throwable>{
    @PostMapping("/create")
    ResponseEntity<RESPONSE_ENTITY> create
            (@RequestBody REQUEST_ENTITY request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) throws UserNotFoundException;
    @GetMapping("/{id}")
    ResponseEntity<RESPONSE_ENTITY> readOne (@PathVariable RequestId id) throws EXCEPTION;
    @GetMapping ("/")
    List<RESPONSE_ENTITY> readAll();
    @PutMapping("/update/{id}")
    ResponseEntity<RESPONSE_ENTITY> update(@PathVariable RequestId id, @RequestBody REQUEST_ENTITY entity) throws EXCEPTION;
    @DeleteMapping("/delete/{id}")
    ResponseEntity<RESPONSE_ENTITY> delete(@PathVariable RequestId id) throws EXCEPTION;
}
