package com.example.app.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import java.time.Instant;
import java.util.*;

@Validated
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "User")
@Table(name="_user")

public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;
    @Size(min = 8, max = 20)
    private String password;
    private String firstname;
    private String lastname;
    private String email;
    private String specialization;
    private String currentProject;
    private Instant registerDate;
    private Instant lastLogin;
    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(name="user_event",
            joinColumns={@JoinColumn(name="user_id")},
            inverseJoinColumns ={@JoinColumn(name="event_id")}
    )
    @Builder.Default
    // for every instance of this user object we have a set of events that user HAS
    private Set<Event> userHasEvents = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
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
    public boolean isCredentialsNonExpired() {return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
