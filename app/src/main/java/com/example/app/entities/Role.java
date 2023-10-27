package com.example.app.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.app.entities.Permission.*;

@RequiredArgsConstructor
public enum Role {
    USER(Set.of(
            USER_READ_ONE,
            USER_READ_ALL,
            READ_USER_EVENTS,
            LEAVE_CREATE,
            LEAVE_READ_ONE,
            LEAVE_READ_ALL,
            LEAVE_UPDATE,
            LEAVE_DELETE,
            FILE_UPLOAD,
            FILE_DOWNLOAD_EVALUATION,
            FILE_DOWNLOAD_TIMESHEET,
            FILE_READ_ALL_EVALUATIONS,
            FILE_READ_ALL_TIMESHEETS,
            FILE_DELETE
            )
    ),
    HR(Set.of(
            USER_READ_ONE,
            USER_READ_ALL,
            READ_USER_EVENTS,
            EVENT_CREATE,
            EVENT_CREATE_BY_GROUP,
            EVENT_READ_ONE,
            EVENT_READ_ALL,
            EVENT_UPDATE,
            EVENT_DELETE,
            ADD_USERS_TO_EVENT,
            REMOVE_USERS_FROM_EVENT,
            PATCH_EVENT_DETAILS,
            LEAVE_READ_ONE,
            LEAVE_READ_ALL,
            FILE_DOWNLOAD_TIMESHEET,
            FILE_READ_ALL_TIMESHEETS
    )

    ),
    MANAGER(Set.of(
            USER_READ_ONE,
            USER_READ_ALL,
            READ_USER_EVENTS,
            EVENT_CREATE_BY_GROUP,
            EVENT_READ_ONE,
            EVENT_READ_ALL,
            EVENT_UPDATE,
            EVENT_DELETE,
            ADD_USERS_TO_EVENT,
            REMOVE_USERS_FROM_EVENT,
            PATCH_EVENT_DETAILS,
            LEAVE_READ_ONE,
            LEAVE_READ_ALL,
            APPROVE_LEAVE,
            FILE_DOWNLOAD_EVALUATION,
            FILE_READ_ALL_EVALUATIONS,
            APPROVE_EVALUATION
    )),
    ADMIN(Set.of(
            USER_CREATE,
            USER_READ_ONE,
            USER_READ_ALL,
            USER_UPDATE,
            USER_DELETE,
            USER_PATCH,
            READ_USER_EVENTS,
            EVENT_CREATE,
            EVENT_CREATE_BY_GROUP,
            EVENT_READ_ONE,
            EVENT_READ_ALL,
            EVENT_UPDATE,
            EVENT_DELETE,
            ADD_USERS_TO_EVENT,
            REMOVE_USERS_FROM_EVENT,
            PATCH_EVENT_DETAILS,
            GROUP_CREATE,
            GROUP_READ_ONE,
            GROUP_READ_ALL,
            GROUP_UPDATE,
            GROUP_DELETE,
            LEAVE_CREATE,
            LEAVE_READ_ONE,
            LEAVE_READ_ALL,
            LEAVE_UPDATE,
            LEAVE_DELETE,
            APPROVE_LEAVE,
            FILE_UPLOAD,
            FILE_DOWNLOAD_EVALUATION,
            FILE_DOWNLOAD_TIMESHEET,
            FILE_READ_ALL_TIMESHEETS,
            FILE_READ_ALL_EVALUATIONS,
            FILE_DELETE,
            APPROVE_EVALUATION
    )
    );

    @Getter
    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities(){
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_"+this.name()));
        return authorities;
    }
}
