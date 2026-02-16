package com.footballmoneyball.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Match Entity
 *
 * Represents a football match between two teams.
 * Used to track historical match data for calculating team form.
 *
 * Contains:
 * - Teams playing (home and away)
 * - Final score
 * - Advanced stats (xG, xA, possession, shots)
 * - Match date and completion status
 */
@Entity
@Table(name = "matches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationships to Team entity
    // Many matches can have the same home team
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    // Match result
    @Column(name = "home_score")
    private Integer homeScore;        // Final score: e.g., 3

    @Column(name = "away_score")
    private Integer awayScore;        // Final score: e.g., 1

    // Advanced statistics
    @Column(name = "home_xg")
    private Double homeXg;            // Expected goals for home team

    @Column(name = "away_xg")
    private Double awayXg;            // Expected goals for away team

    @Column(name = "home_xa")
    private Double homeXa;            // Expected assists for home team

    @Column(name = "away_xa")
    private Double awayXa;            // Expected assists for away team

    @Column(name = "home_possession")
    private Double homePossession;    // Possession percentage

    @Column(name = "away_possession")
    private Double awayPossession;

    @Column(name = "home_shots")
    private Integer homeShots;        // Total shots

    @Column(name = "away_shots")
    private Integer awayShots;

    // Match metadata
    @Column(name = "match_date", nullable = false)
    private LocalDateTime matchDate;  // When the match was played

    private Boolean completed;        // Has the match finished?

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (completed == null) {
            completed = false;
        }
    }
}