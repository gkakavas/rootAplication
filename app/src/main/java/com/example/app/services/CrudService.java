package com.example.app.services;

import com.example.app.exception.UserNotFoundException;

import java.util.List;
import java.util.UUID;

public interface CrudService<RESPONSE_ENTITY ,REQUEST_ENTITY,EXCEPTION extends Throwable> {
    RESPONSE_ENTITY create(REQUEST_ENTITY entity, String token) throws UserNotFoundException;
    RESPONSE_ENTITY read(UUID id) throws EXCEPTION;
    List<RESPONSE_ENTITY> read();
    RESPONSE_ENTITY update(UUID id,REQUEST_ENTITY entity) throws EXCEPTION;
    boolean delete(UUID id) throws EXCEPTION;
}
