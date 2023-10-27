package com.example.app.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name="Group")
@Table(name = "groups")
public class Group {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID groupId;
    private String groupName;
    private UUID groupCreator;
    private LocalDateTime groupCreationDate;

    @JsonManagedReference
    @OneToMany(mappedBy = "group",fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @Builder.Default
    private Set<User> groupHasUsers = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        return new EqualsBuilder().append(groupId, group.groupId).append(groupName, group.groupName).append(groupCreator, group.groupCreator).append(groupCreationDate, group.groupCreationDate).append(groupHasUsers, group.groupHasUsers).isEquals();
    }
}
