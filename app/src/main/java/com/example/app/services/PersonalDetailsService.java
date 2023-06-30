package com.example.app.services;

import com.example.app.entities.User;
import com.example.app.models.PersonalDetailsResponse;
import com.example.app.models.UserProfileResponse;
import com.example.app.repositories.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
@Service
@RequiredArgsConstructor
public class PersonalDetailsService{
private final UserRepository userRepo;
private final List<PersonalDetailsResponse> pDR;
    public List<PersonalDetailsResponse> retrieveAllUsers(){
        List<User> users = userRepo.findAll();
        for(User user:users){
            pDR.add(new PersonalDetailsResponse(user.getUserId(),
                    user.getFirstname(),
                    user.getLastname(),
                    user.getEmail()));
        };
        return pDR;
        }

    public UserProfileResponse retrieveUserProfile(Integer id){
        var user = userRepo.findById(id);
        user.ifPresent(->{
            Builder().UserProfileResponse

        }

        );


    }
}
