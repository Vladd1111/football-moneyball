package com.footballmoneyball.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prediction Response DTO
 *
 * What we send back to the user after making a prediction.
 *
 * Example:
 * {
 *   "homeTeamName": "Manchester City",
 *   "awayTeamName": "Arsenal",
 *   "homeWinProbability": 0.523,
 *   "drawProbability": 0.267,
 *   "awayWinProbability": 0.210,
 *   "predictedHomeXg": 2.74,
 *   "predictedAwayXg": 1.77,
 *   "aiAnalysis": "Manchester City holds a significant advantage...",
 *   "confidence": "MEDIUM"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {

    private String homeTeamName;        // "Manchester City"
    private String awayTeamName;        // "Arsenal"

    private Double homeWinProbability;  // 0.523 = 52.3%
    private Double drawProbability;     // 0.267 = 26.7%
    private Double awayWinProbability;  // 0.210 = 21.0%

    private Double predictedHomeXg;     // 2.74 expected goals
    private Double predictedAwayXg;     // 1.77 expected goals

    private String aiAnalysis;          // AI commentary from Gemini
    private String confidence;          // HIGH, MEDIUM, or LOW
}