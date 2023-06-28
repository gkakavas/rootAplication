package com.example.app.services;

import com.example.app.entities.User;
import com.example.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PersonalDetailsService{
private final UserRepository userRepo;
    public List<User> retrieveAllUsers(){
        return userRepo.findAll();
}
    public Optional<User> retrieveAUser(String email){
        return userRepo.findByEmail(email);
    }
}
