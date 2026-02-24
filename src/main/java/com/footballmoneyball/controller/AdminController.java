package com.footballmoneyball.controller;

import com.footballmoneyball.model.Match;
import com.footballmoneyball.service.FootballApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin Controller
 *
 * Administrative endpoints for managing data
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminController {

    private final FootballApiService footballApiService;

    /**
     * Fetch and update fixtures from API-Football
     *
     * GET /api/admin/refresh-fixtures
     *
     * Requires ADMIN role
     */
    @PostMapping("/refresh-fixtures")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> refreshFixtures() {
        log.info("Admin refreshing fixtures from API-Football");

        try {
            List<Match> matches = footballApiService.fetchUpcomingFixtures();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Fixtures refreshed successfully",
                    "count", matches.size()
            ));

        } catch (Exception e) {
            log.error("Failed to refresh fixtures: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Failed to refresh fixtures: " + e.getMessage()
            ));
        }
    }
}