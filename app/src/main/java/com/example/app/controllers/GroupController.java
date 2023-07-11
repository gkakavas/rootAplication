package com.example.app.controllers;

import com.example.app.models.requests.GroupRequestEntity;
import com.example.app.models.responses.GroupResponseEntity;
import com.example.app.services.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
@Slf4j
public class GroupController implements CrudController<GroupResponseEntity,GroupRequestEntity>{
    private final GroupService groupService;
    @Override
    public ResponseEntity<GroupResponseEntity> create(@Valid GroupRequestEntity request, String header) {
        return ResponseEntity.ok(groupService.create(request,header));
    }

    @Override
    public ResponseEntity<GroupResponseEntity> readOne(UUID id) {
        return ResponseEntity.ok(groupService.read(id));
    }

    @Override
    public List<GroupResponseEntity> readAll() {
        return groupService.read();
    }

    @Override
    public ResponseEntity<GroupResponseEntity> update(UUID id, GroupRequestEntity request) {
        return ResponseEntity.ok(groupService.update(id,request));
    }

    @Override
    public ResponseEntity<GroupResponseEntity> delete(UUID id) {
        var isRemoved = groupService.delete(id);

        if (!isRemoved) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
