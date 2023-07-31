package com.example.app.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name="User")
@Table(name="_user")

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
    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(name = "user_event",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "event_id")}
    )
    @Builder.Default
    private Set<Event> userHasEvents = new HashSet<>();

    @ManyToOne
    @JoinColumn(name= "group_id")
    private Group group;

    @OneToMany(mappedBy = "uploadedBy",fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Builder.Default
    private Set<File> userHasFiles = new HashSet<>();

    @OneToMany(mappedBy = "requestedBy", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Leave> userRequestedLeaves = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.toString()));
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
}


