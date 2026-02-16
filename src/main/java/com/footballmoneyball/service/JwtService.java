package com.footballmoneyball.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Service
 *
 * Handles JSON Web Token (JWT) creation and validation.
 *
 * JWT = A secure token that proves user is authenticated
 * Think of it like a digital badge that says "This person logged in successfully"
 *
 * How it works:
 * 1. User logs in with username/password
 * 2. We create a JWT token encrypted with our secret key
 * 3. User includes this token in all future requests
 * 4. We validate the token to confirm they're still logged in
 */
@Service
public class JwtService {

    // Secret key for signing tokens (from application.properties)
    // MUST be at least 256 bits (64 characters in base64)
    @Value("${jwt.secret}")
    private String secretKey;

    // How long until access token expires (24 hours)
    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    // How long until refresh token expires (7 days)
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * Extract username from token
     *
     * When user sends request, we extract their username from the token
     * to know who they are.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract any claim from token
     *
     * Claims = pieces of information stored in the token
     * Like: username, expiration date, etc.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generate access token for user
     *
     * This is the main token used for authentication.
     * Expires after 24 hours.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generate token with extra claims
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Generate refresh token
     *
     * Used to get a new access token when the old one expires.
     * Lasts longer (7 days) than access token.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    /**
     * Build the actual token
     *
     * Structure of JWT:
     * {
     *   "sub": "admin",              // Subject (username)
     *   "iat": 1707854400,           // Issued at (timestamp)
     *   "exp": 1707940800            // Expires at (timestamp)
     * }
     *
     * This gets encrypted with our secret key.
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration) {

        return Jwts
                .builder()
                .setClaims(extraClaims)                          // Extra info
                .setSubject(userDetails.getUsername())          // Username
                .setIssuedAt(new Date(System.currentTimeMillis()))  // Now
                .setExpiration(new Date(System.currentTimeMillis() + expiration))  // Expiry
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)  // Sign with secret
                .compact();  // Create the token string
    }

    /**
     * Validate token
     *
     * Checks:
     * 1. Does the username in token match the user?
     * 2. Has the token expired?
     *
     * If both are OK → token is valid
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Check if token has expired
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract expiration date from token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract all claims from token
     *
     * Parses the JWT and decrypts it using our secret key.
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .setSigningKey(getSignInKey())  // Use our secret key
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Get signing key from secret
     *
     * Converts our base64 secret string into a Key object
     * that can be used for encryption/decryption.
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}