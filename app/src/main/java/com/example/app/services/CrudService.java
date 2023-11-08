package com.example.app.services;

import com.example.app.entities.User;
import com.example.app.exception.GroupNotFoundException;
import com.example.app.exception.UserNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.UUID;

public interface CrudService<RESPONSE_ENTITY ,REQUEST_ENTITY,EXCEPTION extends Throwable> {
    RESPONSE_ENTITY create(REQUEST_ENTITY entity, @AuthenticationPrincipal User connectedUser) throws UserNotFoundException, GroupNotFoundException;
    RESPONSE_ENTITY read(UUID id, @AuthenticationPrincipal User connectedUser) throws EXCEPTION;
    List<RESPONSE_ENTITY> read(@AuthenticationPrincipal User connectedUser);
    RESPONSE_ENTITY update(UUID id,REQUEST_ENTITY entity) throws EXCEPTION;
    boolean delete(UUID id) throws EXCEPTION;
}
