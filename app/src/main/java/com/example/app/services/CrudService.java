package com.example.app.services;

import java.util.List;
import java.util.UUID;

public interface CrudService<RESPONSE_ENTITY ,REQUEST_ENTITY> {
    RESPONSE_ENTITY read(UUID id);
    List<RESPONSE_ENTITY> read();
    RESPONSE_ENTITY update(REQUEST_ENTITY entity);
    void delete(UUID id);
}
