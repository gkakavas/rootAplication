package com.example.app.controllers;

import com.example.app.models.*;
import com.example.app.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;
    @GetMapping("/account")
    public ResponseEntity<PersonalAccountResponse> retrievePersonalAccount(
            @RequestHeader("Authorization") PersonalAccountRequest requestToken){
            return ResponseEntity.ok(service.retrievePersonalAccount(requestToken));
    }
    @GetMapping("/profiles")
    public ResponseEntity<List<PersonalDetailsResponse>> retrieveAllUsers (){
        return ResponseEntity.ok(service.retrieveAllUsers());
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<UserProfileResponse> retrieveUserProfile(@PathVariable Integer id){
        return ResponseEntity.ok(service.retrieveUserProfile(id));
    }

    @DeleteMapping("/profile/delete/{id}")
    public ResponseEntity<Integer> deleteAUser(@PathVariable Integer id){
    return ResponseEntity.ok(service.deleteUser(id));
    }

    @PutMapping("/profile/update/{id}")
    public ResponseEntity<UpdateUserResponse> updateAUser(@PathVariable Integer id,
                                                          @RequestBody UpdateUserRequest request){
        return ResponseEntity.ok(service.updateUser(id,request));
    }
}
