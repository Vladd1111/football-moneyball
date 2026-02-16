package com.footballmoneyball.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Prediction Entity
 *
 * Stores the result of a match prediction.
 * Contains probabilities, expected goals, and AI analysis.
 *
 * Created whenever a user requests a prediction.
 * Used for:
 * - Showing prediction history
 * - Analyzing prediction accuracy
 * - Tracking AI performance
 */
@Entity
@Table(name = "predictions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Store team IDs (not full Team objects to keep it simple)
    @Column(name = "home_team_id", nullable = false)
    private Long homeTeamId;

    @Column(name = "away_team_id", nullable = false)
    private Long awayTeamId;

    // Probabilities (stored as decimals: 0.523 = 52.3%)
    @Column(name = "home_win_prob")
    private Double homeWinProb;       // Probability of home win

    @Column(name = "draw_prob")
    private Double drawProb;          // Probability of draw

    @Column(name = "away_win_prob")
    private Double awayWinProb;       // Probability of away win

    // Expected goals predictions
    @Column(name = "predicted_home_xg")
    private Double predictedHomeXg;   // How many goals home team will score

    @Column(name = "predicted_away_xg")
    private Double predictedAwayXg;   // How many goals away team will score

    // AI-generated analysis from Google Gemini
    @Column(name = "ai_analysis", columnDefinition = "TEXT")
    private String aiAnalysis;        // Expert commentary from AI

    // Confidence level in prediction
    private String confidence;        // HIGH, MEDIUM, LOW

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}