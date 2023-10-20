package com.example.app.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class AccessDeniedController {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @GetMapping("/access-denied")
    public String getAccessDenied(){
        return "/accessDenied";
    }

}
