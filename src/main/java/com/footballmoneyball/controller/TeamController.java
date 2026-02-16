package com.footballmoneyball.controller;

import com.footballmoneyball.model.Team;
import com.footballmoneyball.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Team Controller
 *
 * Handles team-related endpoints:
 * - GET /api/teams - Get all teams
 * - GET /api/teams/{id} - Get team by ID
 * - GET /api/teams/league/{league} - Get teams by league
 * - POST /api/teams - Create new team (ADMIN only)
 * - PUT /api/teams/{id} - Update team (ADMIN only)
 * - DELETE /api/teams/{id} - Delete team (ADMIN only)
 */
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeamController {

    private final TeamRepository teamRepository;

    /**
     * Get all teams
     *
     * GET /api/teams
     * Returns: List of all teams
     *
     * Anyone can access (ADMIN, ANALYST, GUEST)
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST', 'GUEST')")
    public ResponseEntity<List<Team>> getAllTeams() {
        log.info("Fetching all teams");
        List<Team> teams = teamRepository.findAll();
        return ResponseEntity.ok(teams);
    }

    /**
     * Get team by ID
     *
     * GET /api/teams/1
     * Returns: Single team
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST', 'GUEST')")
    public ResponseEntity<Team> getTeamById(@PathVariable Long id) {
        log.info("Fetching team with ID: {}", id);
        return teamRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get teams by league
     *
     * GET /api/teams/league/Premier%20League
     * Returns: List of teams in that league
     */
    @GetMapping("/league/{league}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST', 'GUEST')")
    public ResponseEntity<List<Team>> getTeamsByLeague(@PathVariable String league) {
        log.info("Fetching teams for league: {}", league);
        List<Team> teams = teamRepository.findByLeague(league);
        return ResponseEntity.ok(teams);
    }

    /**
     * Create new team
     *
     * POST /api/teams
     * Body: {
     *   "name": "Real Madrid",
     *   "league": "La Liga",
     *   "averageXg": 2.5,
     *   "wins": 25,
     *   ...
     * }
     *
     * ADMIN only
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Team> createTeam(@RequestBody Team team) {
        log.info("Creating new team: {}", team.getName());
        Team savedTeam = teamRepository.save(team);
        return ResponseEntity.ok(savedTeam);
    }

    /**
     * Update team
     *
     * PUT /api/teams/1
     * Body: {updated team data}
     *
     * ADMIN only
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Team> updateTeam(@PathVariable Long id, @RequestBody Team teamDetails) {
        log.info("Updating team with ID: {}", id);
        return teamRepository.findById(id)
                .map(team -> {
                    team.setName(teamDetails.getName());
                    team.setLeague(teamDetails.getLeague());
                    team.setAverageXg(teamDetails.getAverageXg());
                    team.setAverageXa(teamDetails.getAverageXa());
                    team.setWins(teamDetails.getWins());
                    team.setDraws(teamDetails.getDraws());
                    team.setLosses(teamDetails.getLosses());
                    team.setGoalsScored(teamDetails.getGoalsScored());
                    team.setGoalsConceded(teamDetails.getGoalsConceded());
                    Team updated = teamRepository.save(team);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete team
     *
     * DELETE /api/teams/1
     *
     * ADMIN only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        log.info("Deleting team with ID: {}", id);
        if (teamRepository.existsById(id)) {
            teamRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}