package com.example.app.models.responses;

import com.example.app.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupResponseEntity {
    private UUID groupId;
    private String groupName;
    private UUID groupCreator;
    private LocalDateTime groupCreationDate;
    @Builder.Default
    private Set<User> userSet = new HashSet<>();
}
