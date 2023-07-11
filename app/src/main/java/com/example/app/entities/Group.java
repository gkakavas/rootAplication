package com.example.app.entities;

import jakarta.persistence.*;
import lombok.*;
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
@Table(name = "_group")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID groupId;
    private String groupName;
    private UUID groupCreator;
    private LocalDateTime groupCreationDate;

    @OneToMany(mappedBy = "group")
    @Builder.Default
    private Set<User> groupHasUsers = new HashSet<>();
}
