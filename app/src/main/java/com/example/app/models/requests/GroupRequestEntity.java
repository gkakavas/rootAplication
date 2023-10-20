package com.example.app.models.requests;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupRequestEntity implements RequestEntity {
    private String groupName;
    @Builder.Default
    private Set<UUID> idsSet = new HashSet<>();
}
