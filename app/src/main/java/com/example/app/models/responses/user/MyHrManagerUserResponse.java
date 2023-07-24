package com.example.app.models.responses.user;

import com.example.app.entities.Group;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class MyHrManagerUserResponse extends OtherUserResponse{
//24/7
    private Group group;

    public MyHrManagerUserResponse(UUID userId, String firstname, String lastname, String email, String specialization, String currentProject, Group group) {
        super(userId, firstname, lastname, email, specialization, currentProject);
        this.group = group;
    }
}
