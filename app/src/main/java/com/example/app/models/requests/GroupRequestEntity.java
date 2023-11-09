package com.example.app.models.requests;

import com.example.app.utils.deserializers.UUIDSetDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupRequestEntity implements RequestEntity {
    @NotNull
    @Size(min = 5,max = 30,message = "The group name must be between 5 and 30 characters")
    private String groupName;
    @JsonDeserialize(using = UUIDSetDeserializer.class)
    @Builder.Default
    private Set<UUID> idsSet = new HashSet<>();
}
