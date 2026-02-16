package com.footballmoneyball.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;   // JWT token for authentication
    private String refreshToken;  // Token to get new access token
    private String username;
    private String role;
}