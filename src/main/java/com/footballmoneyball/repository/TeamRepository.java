package com.footballmoneyball.repository;

import com.footballmoneyball.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Team Repository
 *
 * Provides database access methods for Team entity.
 * Spring Data JPA automatically implements these methods!
 *
 * Available methods (auto-generated):
 * - findAll() - Get all teams
 * - findById(id) - Get team by ID
 * - save(team) - Create or update team
 * - delete(team) - Delete team
 * - And any custom methods we declare below
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * Find team by exact name
     * Spring translates to: SELECT * FROM teams WHERE name = ?
     */
    Optional<Team> findByName(String name);

    /**
     * Find all teams in a specific league
     * Spring translates to: SELECT * FROM teams WHERE league = ?
     */
    List<Team> findByLeague(String league);

    /**
     * Find teams by name containing a string (case-insensitive)
     * Spring translates to: SELECT * FROM teams WHERE LOWER(name) LIKE LOWER(?%)
     *
     * Example: findByNameContainingIgnoreCase("united")
     * Returns: Manchester United, Newcastle United, West Ham United
     */
    List<Team> findByNameContainingIgnoreCase(String name);

    /**
     * Check if team exists by name
     * Spring translates to: SELECT COUNT(*) > 0 FROM teams WHERE name = ?
     */
    boolean existsByName(String name);
}