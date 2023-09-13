package com.example.app.utils.common;

import com.example.app.entities.Event;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.responses.common.EventWithUsers;
import com.example.app.models.responses.common.UserWithFiles;
import com.example.app.models.responses.common.UserWithLeaves;
import com.example.app.repositories.UserRepository;
import com.example.app.utils.file.EntityResponseFileConverter;
import com.example.app.utils.leave.EntityResponseLeaveConverter;
import com.example.app.utils.user.EntityResponseUserConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EntityResponseCommonConverterImpl implements EntityResponseCommonConverter{

    private final EntityResponseUserConverter userConverter;
    private final EntityResponseFileConverter fileConverter;
    private final EntityResponseLeaveConverter leaveConverter;
    private final UserDetailsService uds;
    private final UserRepository userRepo;
    @Override
    public Set<UserWithLeaves> usersWithLeaves(Set<User> users) {
        var currentUser = userRepo.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new AccessDeniedException("You have not authority to access this resource"));
        Set<UserWithLeaves> userWithLeavesList = new HashSet<>();
        if (currentUser.getRole().equals(Role.ADMIN)) {
            for (User user : users) {
                user.getUserRequestedLeaves().forEach((leave) -> userWithLeavesList.add(UserWithLeaves.builder()
                        .user(userConverter.fromUserToAdminUser(user))
                        .leaves(leaveConverter.fromLeaveListToAdminHrMngLeaveList(user.getUserRequestedLeaves()))
                        .build()));
            }
        }
        else if (Arrays.asList(Role.HR,Role.MANAGER).contains(currentUser.getRole())) {
            for (User user : users) {
                user.getUserRequestedLeaves().forEach((leave) -> userWithLeavesList.add(UserWithLeaves.builder()
                        .user(userConverter.fromUserToOtherUser(user))
                        .leaves(leaveConverter.fromLeaveListToAdminHrMngLeaveList(user.getUserRequestedLeaves()))
                        .build()));
            }
        }
        else if (currentUser.getRole().equals(Role.USER)) {
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
    public Set<UserWithFiles> usersWithFilesList(Set<User> users) throws UserNotFoundException {
        var currentUser = userRepo.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(UserNotFoundException::new);
        Set<UserWithFiles> userWithFilesList = new HashSet<>();
        if(currentUser.getRole().equals(Role.ADMIN)){
            for(User user:users){
                user.getUserHasFiles().forEach((file) -> userWithFilesList.add(UserWithFiles.builder()
                                .user(userConverter.fromUserToAdminUser(user))
                                .files(fileConverter.fromFileListToAdminList(user.getUserHasFiles()))
                        .build()));
            }
            return userWithFilesList;
        }
        else if(Arrays.asList(Role.HR,Role.MANAGER,Role.USER).contains(currentUser.getRole())){
            for(User user:users){
                user.getUserHasFiles().forEach((file) -> userWithFilesList.add(UserWithFiles.builder()
                        .user(userConverter.fromUserToOtherUser(user))
                        .files(fileConverter.fromFileListToUserFileList(user.getUserHasFiles()))
                        .build()));
            }
            return userWithFilesList;
        }
        else throw new AccessDeniedException("You have not authority to access this resource");

    }

    @Override
    public Set<EventWithUsers> eventsWithUsersList(Set<Event> events){
        return null;
    }
}
