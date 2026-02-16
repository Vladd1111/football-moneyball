package com.footballmoneyball.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * User Entity
 *
 * Represents a user who can log in to the system.
 * Implements UserDetails for Spring Security integration.
 *
 * Roles:
 * - ADMIN: Full access (can manage teams, users, view all predictions)
 * - ANALYST: Can view all predictions and make predictions
 * - GUEST: Can only make predictions
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;      // BCrypt encrypted password

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;                // ADMIN, ANALYST, or GUEST

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Enum for user roles
    public enum Role {
        ADMIN, ANALYST, GUEST
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Spring Security UserDetails interface methods

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert role to Spring Security authority
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Our accounts don't expire
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // We don't lock accounts
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Passwords don't expire
    }

    @Override
    public boolean isEnabled() {
        return true;  // All users are enabled
    }
}