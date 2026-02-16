package com.footballmoneyball.security;

import com.footballmoneyball.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Extract Authorization header
        final String authHeader = request.getHeader("Authorization");

        log.debug("Auth header: {}", authHeader);  // ADD THIS LINE FOR DEBUGGING

        // If no header or doesn't start with "Bearer ", skip
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found, skipping JWT filter");  // ADD THIS
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token
        final String jwt = authHeader.substring(7);

        log.debug("Extracted JWT: {}", jwt.substring(0, Math.min(20, jwt.length())) + "...");  // ADD THIS

        try {
            // Extract username from token
            final String username = jwtService.extractUsername(jwt);

            log.debug("Username from token: {}", username);  // ADD THIS

            // If user not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load user details
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Validate token
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    log.info("Token is valid for user: {}", username);  // ADD THIS

                    // Create authentication
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Set authentication
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.info("User {} authenticated successfully", username);
                } else {
                    log.warn("Token validation failed for user: {}", username);  // ADD THIS
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}