package com.footballmoneyball.controller;

import com.footballmoneyball.dto.AuthResponse;
import com.footballmoneyball.dto.LoginRequest;
import com.footballmoneyball.dto.SignupRequest;
import com.footballmoneyball.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 *
 * Handles user authentication endpoints:
 * - POST /api/auth/signup - Register new user
 * - POST /api/auth/login - Login existing user
 * - POST /api/auth/refresh - Refresh access token
 * - POST /api/auth/logout - Logout user
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Sign up new user
     *
     * POST /api/auth/signup
     * Body: {
     *   "username": "john",
     *   "password": "password123",
     *   "role": "GUEST"
     * }
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        log.info("Signup request for username: {}", request.getUsername());
        AuthResponse response = authenticationService.signup(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Login existing user
     *
     * POST /api/auth/login
     * Body: {
     *   "username": "admin",
     *   "password": "admin123"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("Login request for username: {}", request.getUsername());
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token
     *
     * POST /api/auth/refresh
     * Body: {
     *   "refreshToken": "eyJhbGci..."
     * }
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request");
        // Use record accessor: refreshToken() not getRefreshToken()
        AuthResponse response = authenticationService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Logout user
     *
     * POST /api/auth/logout
     * Body: {
     *   "username": "admin"
     * }
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutRequest request) {
        log.info("Logout request for username: {}", request.username());
        // Use record accessor: username() not getUsername()
        authenticationService.logout(request.username());
        return ResponseEntity.ok("Logged out successfully");
    }

    // Helper record classes for requests
    // Records use accessor methods: refreshToken() and username()
    // NOT getRefreshToken() and getUsername()
    record RefreshTokenRequest(String refreshToken) {}
    record LogoutRequest(String username) {}
}