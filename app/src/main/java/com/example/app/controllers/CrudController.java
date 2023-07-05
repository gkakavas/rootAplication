package com.example.app.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RequestMapping("/Default")
public interface CrudController <RESPONSE_ENTITY ,REQUEST_ENTITY>{
    @PostMapping("/create/")
    ResponseEntity<RESPONSE_ENTITY> create(@RequestBody REQUEST_ENTITY entity);
    @GetMapping("/{id}")
    ResponseEntity<RESPONSE_ENTITY> readOne(@PathVariable UUID id);
    @GetMapping ("/")
    List<RESPONSE_ENTITY> readAll();
    @PutMapping("/update/{id}")
    ResponseEntity<RESPONSE_ENTITY> update(@PathVariable UUID id, @RequestBody REQUEST_ENTITY entity);
    @DeleteMapping("/delete/{id}")
    ResponseEntity<RESPONSE_ENTITY> delete(@PathVariable UUID id);
}
