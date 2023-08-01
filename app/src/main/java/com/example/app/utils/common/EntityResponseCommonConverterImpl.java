package com.example.app.utils.common;

import com.example.app.entities.Event;
import com.example.app.entities.User;
import com.example.app.models.responses.common.EventWithUsers;
import com.example.app.models.responses.common.UserWithFiles;
import com.example.app.models.responses.common.UserWithLeaves;
import com.example.app.utils.file.EntityResponseFileConverter;
import com.example.app.utils.leave.EntityResponseLeaveConverter;
import com.example.app.utils.user.EntityResponseUserConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EntityResponseCommonConverterImpl implements EntityResponseCommonConverter{

    private final EntityResponseUserConverter userConverter;
    private final EntityResponseFileConverter fileConverter;
    private final EntityResponseLeaveConverter leaveConverter;
    private final UserDetailsService uds;
    @Override
    public Set<UserWithLeaves> usersWithLeaves(Set<User> users) {
        UserDetails userDetails = uds.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Set<UserWithLeaves> userWithLeavesList = new HashSet<>();

        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ROLE_ADMIN")) ||
                userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ROLE_MANAGER")) ||
                userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ROLE_HR"))) {
            for (User user : users) {
                user.getUserRequestedLeaves().forEach((leave) -> userWithLeavesList.add(UserWithLeaves.builder()
                        .user(userConverter.fromUserToAdminUser(user))
                        .leaves(leaveConverter.fromLeaveListToAdminHrMngLeaveList(user.getUserRequestedLeaves()))
                        .build()));
            }
        }
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ROLE_USER"))) {
            for (User user : users) {
                user.getUserRequestedLeaves().forEach((leave) -> userWithLeavesList.add(UserWithLeaves.builder()
                        .user(userConverter.fromUserToOtherUser(user))
                        .leaves(leaveConverter.fromLeaveListToMyLeaveList(user.getUserRequestedLeaves()))
                        .build()));
            }

        }
        return userWithLeavesList;
    }


    @Override
    public Set<UserWithFiles> usersWithFilesList(Set<User> users) {

        UserDetails userDetails = uds.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Set<UserWithFiles> userWithFilesList = new HashSet<>();

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

    @Override
    public Set<EventWithUsers> eventsWithUsersList(Set<Event> events){
        return null;
    }
}
