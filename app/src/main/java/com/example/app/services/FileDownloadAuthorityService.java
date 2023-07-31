package com.example.app.services;

import com.example.app.entities.FileKind;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.FileNotFoundException;
import com.example.app.exception.UserNotFoundException;
import com.example.app.repositories.FileRepository;
import com.example.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileDownloadAuthorityService {
    private final FileRepository fileRepo;
    private final UserRepository userRepo;
    public Path checkAuthority(UUID fileId) throws FileNotFoundException, UserNotFoundException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var file = fileRepo.findById(fileId).orElseThrow(FileNotFoundException::new);
        var user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow(UserNotFoundException::new);
        if(user.getRole().equals(Role.ROLE_ADMIN)){
            return Path.of(file.getAccessUrl());
        }
        else if(user.getRole().equals(Role.ROLE_HR)&&file.getFileKind().equals(FileKind.TIMESHEET)){
            return Path.of(file.getAccessUrl());
        }
        else if(user.getRole().equals(Role.ROLE_MANAGER)&&file.getUploadedBy().getGroup().equals(user.getGroup())){
            return Path.of(file.getAccessUrl());
        }
        else if(user.equals(file.getUploadedBy())&&user.getRole().equals(Role.ROLE_USER)){
            return Path.of(file.getAccessUrl());
        }
        else throw new AccessDeniedException("You have not the authority to access this recourse");
    }
}
