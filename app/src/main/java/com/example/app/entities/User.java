package com.example.app.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString(exclude = {"userHasEvents","userHasFiles","userRequestedLeaves"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name="User")
@Table(name="users")
@Slf4j
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @NotNull
    private UUID userId;
    private String password;
    private String firstname;
    private String lastname;
    @Column(unique = true)
    private String email;
    private String specialization;
    private String currentProject;
    private UUID createdBy;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLogin;
    private String roleValue;
    @Transient
    private Role role;
    @PostLoad
    void fillRole() {
        if(this.roleValue!=null) {
            this.role = Role.valueOf(roleValue);
        }
    }
    @PrePersist
    void fillPersistent() {
        if(this.role!=null) {
            this.roleValue = role.name();
        }
    }
    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name= "group_id")
    private Group group;

    @ManyToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinTable(name = "user_event_mapping",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "event_id")}
    )
    @Builder.Default
    private Set<Event> userHasEvents = new HashSet<>();

    @OneToMany(mappedBy = "uploadedBy",fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @Builder.Default
    private Set<File> userHasFiles = new HashSet<>();

    @OneToMany(mappedBy = "requestedBy",fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @Builder.Default
    private Set<Leave> userRequestedLeaves = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return new EqualsBuilder().append(userId, user.userId).append(password, user.password).append(firstname, user.firstname).append(lastname, user.lastname).append(email, user.email).append(specialization, user.specialization).append(currentProject, user.currentProject).append(createdBy, user.createdBy).append(registerDate, user.registerDate).append(lastLogin, user.lastLogin).append(roleValue, user.roleValue).append(role, user.role).append(userHasEvents, user.userHasEvents).append(group, user.group).append(userHasFiles, user.userHasFiles).append(userRequestedLeaves, user.userRequestedLeaves).isEquals();
    }
}


