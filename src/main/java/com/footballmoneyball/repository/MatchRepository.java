package com.footballmoneyball.repository;

import com.footballmoneyball.model.Match;
import com.footballmoneyball.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Match Repository
 *
 * Provides database access methods for Match entity.
 * Includes custom queries for getting team match history.
 */
@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    /**
     * Find all matches where a team played (either home or away)
     * Ordered by most recent first
     */
    List<Match> findByHomeTeamOrAwayTeamOrderByMatchDateDesc(Team homeTeam, Team awayTeam);

    /**
     * Custom query: Get last 10 completed matches for a specific team
     *
     * This is used to calculate team form (recent performance).
     * We check both home_team_id and away_team_id because a team
     * can play either at home or away.
     *
     * JPQL Query breakdown:
     * - SELECT m FROM Match m: Select from Match entity
     * - WHERE (m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId): Team played either home or away
     * - AND m.completed = true: Only finished matches
     * - ORDER BY m.matchDate DESC: Most recent first
     *
     * Note: We limit to 10 in the service layer
     */
    @Query("SELECT m FROM Match m WHERE (m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId) AND m.completed = true ORDER BY m.matchDate DESC")
    List<Match> findLast10MatchesByTeam(@Param("teamId") Long teamId);

    /**
     * Find all completed matches
     */
    List<Match> findByCompletedTrue();

    /**
     * Find all upcoming matches (not completed)
     */
    List<Match> findByCompletedFalse();
}