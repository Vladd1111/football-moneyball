package com.footballmoneyball.service;

import com.footballmoneyball.dto.AuthResponse;
import com.footballmoneyball.dto.LoginRequest;
import com.footballmoneyball.dto.SignupRequest;
import com.footballmoneyball.model.User;
import com.footballmoneyball.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Authentication Service
 *
 * Handles user signup and login.
 *
 * Flow:
 * 1. Signup: Create new user, hash password, save to DB
 * 2. Login: Validate credentials, generate JWT tokens
 * 3. Refresh: Get new access token using refresh token
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Register a new user
     *
     * Steps:
     * 1. Check if username already exists
     * 2. Hash the password (NEVER store plain passwords!)
     * 3. Create user entity
     * 4. Save to database
     * 5. Generate JWT tokens
     * 6. Return tokens to user
     */
    public AuthResponse signup(SignupRequest request) {

        // Check if username already taken
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }

        log.info("Creating new user: {}", request.getUsername());

        // Hash password using BCrypt
        // "password123" → "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl..."
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create user entity
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(hashedPassword)
                .role(User.Role.valueOf(request.getRole()))  // Convert string to enum
                .build();

        // Save to database
        userRepository.save(user);
        log.info("User created successfully: {}", user.getUsername());

        // Generate JWT tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Store refresh token in Redis (expires in 7 days)
        storeRefreshToken(user.getUsername(), refreshToken);

        // Return tokens to user
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Login existing user
     *
     * Steps:
     * 1. Validate username and password
     * 2. If correct, generate JWT tokens
     * 3. Return tokens to user
     */
    public AuthResponse login(LoginRequest request) {

        log.info("Login attempt for user: {}", request.getUsername());

        // Authenticate user
        // This throws exception if username/password is wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // If we get here, authentication succeeded!
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Login successful for user: {}", user.getUsername());

        // Generate tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Store refresh token in Redis
        storeRefreshToken(user.getUsername(), refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Refresh access token
     *
     * When access token expires (after 24 hours),
     * user can use refresh token to get a new one
     * without logging in again.
     */
    public AuthResponse refreshToken(String refreshToken) {

        // Extract username from refresh token
        String username = jwtService.extractUsername(refreshToken);

        // Get user from database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify refresh token is valid
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Check refresh token exists in Redis
        String storedToken = redisTemplate.opsForValue().get("refresh_token:" + username);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new RuntimeException("Refresh token not found or doesn't match");
        }

        log.info("Refreshing token for user: {}", username);

        // Generate new access token
        String newAccessToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)  // Keep same refresh token
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Logout user
     *
     * Remove refresh token from Redis so it can't be used anymore.
     */
    public void logout(String username) {
        log.info("Logging out user: {}", username);
        redisTemplate.delete("refresh_token:" + username);
    }

    /**
     * Store refresh token in Redis
     *
     * Redis = fast in-memory storage
     * Perfect for storing temporary tokens
     *
     * Key: "refresh_token:admin"
     * Value: "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
     * Expires: 7 days
     */
    private void storeRefreshToken(String username, String refreshToken) {
        String key = "refresh_token:" + username;
        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                7,
                TimeUnit.DAYS
        );
        log.debug("Stored refresh token in Redis for user: {}", username);
    }
}