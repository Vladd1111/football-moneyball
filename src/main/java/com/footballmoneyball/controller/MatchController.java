package com.footballmoneyball.controller;

import com.footballmoneyball.model.Match;
import com.footballmoneyball.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Match Controller
 *
 * Handles match-related endpoints:
 * - GET /api/matches/upcoming - Get upcoming fixtures
 * - GET /api/matches/{id} - Get match by ID
 * - GET /api/matches - Get all matches
 */
@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MatchController {

    private final MatchRepository matchRepository;

    /**
     * Get upcoming matches (not completed)
     *
     * GET /api/matches/upcoming
     * Returns: List of upcoming fixtures ordered by date
     */
    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST', 'GUEST')")
    public ResponseEntity<List<Match>> getUpcomingMatches() {
        log.info("Fetching upcoming matches");
        List<Match> matches = matchRepository.findByCompletedFalse();
        return ResponseEntity.ok(matches);
    }

    /**
     * Get match by ID
     *
     * GET /api/matches/1
     * Returns: Single match
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST', 'GUEST')")
    public ResponseEntity<Match> getMatchById(@PathVariable Long id) {
        log.info("Fetching match with ID: {}", id);
        return matchRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all matches
     *
     * GET /api/matches
     * Returns: List of all matches
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST', 'GUEST')")
    public ResponseEntity<List<Match>> getAllMatches() {
        log.info("Fetching all matches");
        List<Match> matches = matchRepository.findAll();
        return ResponseEntity.ok(matches);
    }
}