package com.example.app.services;

import com.example.app.entities.File;
import com.example.app.entities.FileKind;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.FileNotFoundException;
import com.example.app.repositories.FileRepository;
import com.example.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimesheetFileAuthorityCheckService {
    private final FileRepository fileRepo;
    private final UserDetailsService uDService;
    public Path checkTimesheetAuthority(UUID fileId)throws FileNotFoundException{
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var file = fileRepo.findById(fileId).orElseThrow(FileNotFoundException::new);
        User user = (User) uDService.loadUserByUsername(userDetails.getUsername());

        if(user.getRole().equals(Role.ROLE_MANAGER)||user.getRole().equals(Role.ROLE_HR)&&file.getFileKind().equals(FileKind.TIMESHEET)){
            return Path.of(file.getAccessUrl());
        }
        else if(user.getUserId().equals(file.getUploadedBy().getUserId())&&user.getRole().equals(Role.ROLE_USER)&&file.getFileKind().equals(FileKind.TIMESHEET)){
            return Path.of(file.getAccessUrl());
        }
        else throw new AccessDeniedException("You have not the authority to access this recourse");
    }

    public List<> checkTimesheetAuthority(){

    }
}
