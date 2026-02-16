package com.footballmoneyball.repository;

import com.footballmoneyball.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository
 *
 * Provides database access methods for User entity.
 * Used for authentication and user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     * Used during login
     *
     * Spring translates to: SELECT * FROM users WHERE username = ?
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if username already exists
     * Used during signup to prevent duplicate usernames
     *
     * Spring translates to: SELECT COUNT(*) > 0 FROM users WHERE username = ?
     */
    boolean existsByUsername(String username);
}