package com.footballmoneyball.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test Controller
 *
 * Simple endpoints for testing without authentication
 */
@RestController
public class TestController {

    @GetMapping("/")
    public String home() {
        return "⚽ Football Moneyball API is running! Visit /actuator/health for health check.";
    }

    @GetMapping("/test")
    public String test() {
        return "✅ Test endpoint works! Backend is operational.";
    }
}