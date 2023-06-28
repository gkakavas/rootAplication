package com.example.app.controllers;

import com.example.app.entities.User;
import com.example.app.services.PersonalDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {
    private final PersonalDetailsService service;
    @GetMapping("/profile")
    public List<User> retrieveAllUsers (){
        return service.retrieveAllUsers();
    }

}
