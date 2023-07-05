package com.example.app.services;

import java.util.List;
import java.util.UUID;

public interface CrudService<RESPONSE_ENTITY ,REQUEST_ENTITY> {
    RESPONSE_ENTITY create(REQUEST_ENTITY entity);
    RESPONSE_ENTITY read(UUID id);
    List<RESPONSE_ENTITY> read();
    RESPONSE_ENTITY update(UUID id,REQUEST_ENTITY entity);
    boolean delete(UUID id);
}
