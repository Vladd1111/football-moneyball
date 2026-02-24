package com.footballmoneyball.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Comprehensive betting markets response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BettingMarkets {

    // Basic match outcome
    private MatchOutcome matchOutcome;

    // Over/Under goals
    private Map<String, MarketOdds> overUnder;

    // Both Teams to Score
    private BothTeamsToScore bothTeamsToScore;

    // Double Chance
    private Map<String, MarketOdds> doubleChance;

    // Correct Score (Top 10)
    private List<CorrectScore> topCorrectScores;

    // Additional info
    private String confidence;
    private String aiAnalysis;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchOutcome {
        private MarketOdds homeWin;
        private MarketOdds draw;
        private MarketOdds awayWin;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketOdds {
        private double probability;      // 0.523
        private double percentage;       // 52.3
        private double decimalOdds;      // 1.91
        private String fractionalOdds;   // "10/11"
        private String americanOdds;     // "-110"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BothTeamsToScore {
        private MarketOdds yes;  // GG
        private MarketOdds no;   // NG
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CorrectScore {
        private int homeGoals;
        private int awayGoals;
        private double probability;
        private double percentage;
        private double decimalOdds;
        private int rank;
    }
}