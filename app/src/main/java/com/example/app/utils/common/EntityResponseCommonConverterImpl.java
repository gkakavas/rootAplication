package com.example.app.utils.common;

import com.example.app.entities.User;
import com.example.app.models.responses.common.UserWithFiles;
import com.example.app.models.responses.common.UserWithLeaves;
import com.example.app.utils.file.EntityResponseFileConverter;
import com.example.app.utils.user.EntityResponseUserConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EntityResponseCommonConverterImpl implements EntityResponseCommonConverter{

    private final EntityResponseUserConverter userConverter;
    private final EntityResponseFileConverter fileConverter;
    private final UserDetailsService uds;
    @Override
    public List<UserWithLeaves> usersWithLeaves(List<User> users) {
//        UserDetails userDetails = uds.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
//        List<UserWithFiles> userWithFilesList = new ArrayList<>();
//
//        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ROLE_ADMIN")) ||
//                userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ROLE_MANAGER")) ||
//                userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ROLE_HR"))) {
//            for (User user : users) {
//                user.getUserHasFiles().forEach((file) -> userWithFilesList.add(UserWithFiles.builder()
//                        .user(userConverter.fromUserToAdminUser(user))
//                        .files(fileConverter.fromFileListToAdminList(user.getUserHasFiles()))
//                        .build()));
//            }
//        }
//        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ROLE_USER"))) {
//            for (User user : users) {
//                user.getUserHasFiles().forEach((file) -> userWithFilesList.add(UserWithFiles.builder()
//                        .user(userConverter.fromUserToOtherUser(user))
//                        .files(fileConverter.fromFileListToUserFileList(user.getUserHasFiles()))
//                        .build()));
//            }
//
//        }
        return null;
    }


    @Override
    public List<UserWithFiles> usersWithFilesList(List<User> users) {

        UserDetails userDetails = uds.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        List<UserWithFiles> userWithFilesList = new ArrayList<>();

        if(userDetails.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("ROLE_ADMIN"))||
           userDetails.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("ROLE_MANAGER"))||
           userDetails.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("ROLE_HR"))){
            for(User user:users){
                user.getUserHasFiles().forEach((file) -> userWithFilesList.add(UserWithFiles.builder()
                                .user(userConverter.fromUserToAdminUser(user))
                                .files(fileConverter.fromFileListToAdminList(user.getUserHasFiles()))
                        .build()));
            }
        }
        if(userDetails.getAuthorities().stream().anyMatch(a->a.getAuthority().contains("ROLE_USER"))){
            for(User user:users){
                user.getUserHasFiles().forEach((file) -> userWithFilesList.add(UserWithFiles.builder()
                        .user(userConverter.fromUserToOtherUser(user))
                        .files(fileConverter.fromFileListToUserFileList(user.getUserHasFiles()))
                        .build()));
            }
        }
        return userWithFilesList;
    }
}
