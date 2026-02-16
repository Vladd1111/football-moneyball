package com.footballmoneyball.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Team Entity
 *
 * Represents a football team in the database.
 * Maps to the 'teams' table.
 *
 * Contains team statistics used for predictions:
 * - Basic info: name, league
 * - Season stats: wins, draws, losses, goals
 * - Advanced stats: xG (expected goals), xA (expected assists)
 */
@Entity
@Table(name = "teams")
@Data                    // Lombok: generates getters, setters, toString, equals, hashCode
@Builder                 // Lombok: allows Team.builder().name("Arsenal").build()
@NoArgsConstructor      // Lombok: generates empty constructor
@AllArgsConstructor     // Lombok: generates constructor with all fields
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;              // "Manchester City"

    @Column(nullable = false)
    private String league;            // "Premier League"

    @Column(name = "goals_scored")
    private Integer goalsScored;      // Total goals scored this season

    @Column(name = "goals_conceded")
    private Integer goalsConceded;    // Total goals conceded this season

    @Column(name = "average_xg")
    private Double averageXg;         // Average expected goals per match

    @Column(name = "average_xa")
    private Double averageXa;         // Average expected assists per match

    private Integer wins;             // Number of wins
    private Integer draws;            // Number of draws
    private Integer losses;           // Number of losses

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}