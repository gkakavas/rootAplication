package com.example.app.utils.common;

import com.example.app.entities.Event;
import com.example.app.entities.User;
import com.example.app.models.responses.common.EventWithUsers;
import com.example.app.models.responses.common.UserWithFiles;
import com.example.app.models.responses.common.UserWithLeaves;

import java.util.List;
import java.util.Set;

public interface EntityResponseCommonConverter {
    Set<UserWithLeaves> usersWithLeaves(Set<User> users);
    Set<UserWithFiles> usersWithFilesList(Set<User> users);
    Set<EventWithUsers> eventsWithUsersList(Set<Event> events);
}
