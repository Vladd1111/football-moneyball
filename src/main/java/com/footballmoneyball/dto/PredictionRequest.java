package com.footballmoneyball.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prediction Request DTO
 *
 * What the user sends when requesting a prediction.
 *
 * Example:
 * {
 *   "homeTeamId": 1,
 *   "awayTeamId": 2,
 *   "includeAiAnalysis": true
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequest {

    private Long homeTeamId;           // ID of home team (e.g., 1 for Man City)
    private Long awayTeamId;           // ID of away team (e.g., 2 for Arsenal)
    private Boolean includeAiAnalysis; // Should we get AI commentary?
}