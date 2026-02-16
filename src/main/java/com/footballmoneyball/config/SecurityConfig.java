package com.footballmoneyball.config;

import com.footballmoneyball.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration
 *
 * Configures Spring Security with JWT authentication.
 *
 * PUBLIC endpoints (no login required):
 * - / and /test (home pages)
 * - /api/auth/** (login, signup, refresh, logout)
 * - /actuator/** (health checks)
 *
 * PROTECTED endpoints (JWT token required):
 * - /api/teams/** (all team operations)
 * - /api/predictions/** (all prediction operations)
 * - /api/matches/** (all match operations)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Enables @PreAuthorize annotations in controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (we use JWT tokens, not cookies)
                .csrf(AbstractHttpConfigurer::disable)

                // Configure which endpoints are public vs protected
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC endpoints - anyone can access
                        .requestMatchers(
                                "/",                      // Home page
                                "/test",                  // Test endpoint
                                "/api/auth/login",        // Login
                                "/api/auth/signup",       // Signup
                                "/api/auth/refresh",      // Token refresh
                                "/api/auth/logout",       // Logout
                                "/actuator/**",           // Health checks
                                "/error"                  // Error page
                        ).permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Session management: STATELESS (no server-side sessions)
                // We use JWT tokens instead
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Set our custom authentication provider
                .authenticationProvider(authenticationProvider)

                // Add JWT filter before Spring Security's default authentication filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}