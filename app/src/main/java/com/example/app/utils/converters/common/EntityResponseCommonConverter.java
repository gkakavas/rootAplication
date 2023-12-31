package com.example.app.utils.converters.common;

import com.example.app.entities.Event;
import com.example.app.entities.User;
import com.example.app.exception.UserNotFoundException;
import com.example.app.models.responses.common.EventWithUsers;
import com.example.app.models.responses.common.UserWithFiles;
import com.example.app.models.responses.common.UserWithLeaves;

import java.util.List;
import java.util.Set;

public interface EntityResponseCommonConverter {
    Set<UserWithLeaves> usersWithLeaves(Set<User> users);
    Set<UserWithFiles> usersWithFilesList(Set<User> users) throws UserNotFoundException;
    Set<EventWithUsers> eventsWithUsersList(Set<Event> events);
}
