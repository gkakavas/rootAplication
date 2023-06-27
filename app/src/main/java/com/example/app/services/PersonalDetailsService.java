package com.example.app.services;

import com.example.app.entities.User;
import com.example.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PersonalDetailsService{
private final UserRepository userRepo;
    public List<User> retrieveAllUsers(){
        return userRepo.findAll();
}

}
