package com.footballmoneyball.service;

import com.footballmoneyball.dto.PredictionRequest;
import com.footballmoneyball.dto.PredictionResponse;
import com.footballmoneyball.model.Match;
import com.footballmoneyball.model.Prediction;
import com.footballmoneyball.model.Team;
import com.footballmoneyball.repository.MatchRepository;
import com.footballmoneyball.repository.PredictionRepository;
import com.footballmoneyball.repository.TeamRepository;
import com.footballmoneyball.service.GeminiAIService.MatchProbabilities;
import com.footballmoneyball.service.GeminiAIService.TeamForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Prediction Service - The Brain of Football Moneyball
 *
 * This is where the MAGIC happens! 🎯
 *
 * ALGORITHM OVERVIEW:
 * 1. Get teams from database
 * 2. Calculate recent form (last 10 matches)
 * 3. Predict expected goals (xG) using statistical model
 * 4. Calculate win/draw/loss probabilities using Poisson distribution
 * 5. Get AI analysis from Google Gemini (optional)
 * 6. Save prediction to database
 * 7. Return results to user
 *
 * Mathematical Foundation:
 * - Team Form: Weighted average of recent performance
 * - xG Prediction: (offensive × defensive × form) + home advantage
 * - Probabilities: Poisson distribution over all possible scorelines
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionService {

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final GeminiAIService geminiAIService;

    /**
     * Main prediction method
     *
     * This is what gets called when a user requests a prediction.
     * It orchestrates all the steps to generate a complete prediction.
     */
    @Transactional
    public PredictionResponse predictMatch(PredictionRequest request) {

        // STEP 1: Get teams from database
        Team homeTeam = teamRepository.findById(request.getHomeTeamId())
                .orElseThrow(() -> new RuntimeException("Home team not found with ID: " + request.getHomeTeamId()));
        Team awayTeam = teamRepository.findById(request.getAwayTeamId())
                .orElseThrow(() -> new RuntimeException("Away team not found with ID: " + request.getAwayTeamId()));

        log.info("Predicting match: {} vs {}", homeTeam.getName(), awayTeam.getName());

        // STEP 2: Get recent match history for both teams
        List<Match> homeTeamMatches = matchRepository.findLast10MatchesByTeam(homeTeam.getId());
        List<Match> awayTeamMatches = matchRepository.findLast10MatchesByTeam(awayTeam.getId());

        // STEP 3: Calculate team form from recent matches
        TeamForm homeForm = calculateTeamForm(homeTeam, homeTeamMatches, true);
        TeamForm awayForm = calculateTeamForm(awayTeam, awayTeamMatches, false);

        // STEP 4: Predict expected goals for this match
        double predictedHomeXg = calculateExpectedGoals(homeForm, awayForm, true);
        double predictedAwayXg = calculateExpectedGoals(awayForm, homeForm, false);

        log.info("Predicted xG: {} = {}, {} = {}",
                homeTeam.getName(), predictedHomeXg,
                awayTeam.getName(), predictedAwayXg);

        // STEP 5: Calculate win/draw/loss probabilities using Poisson distribution
        MatchProbabilities probs = calculateMatchProbabilities(predictedHomeXg, predictedAwayXg);

        log.info("Probabilities: Home={}, Draw={}, Away={}",
                probs.homeWin, probs.draw, probs.awayWin);

        // STEP 6: Create prediction entity to save
        Prediction prediction = Prediction.builder()
                .homeTeamId(homeTeam.getId())
                .awayTeamId(awayTeam.getId())
                .homeWinProb(probs.homeWin)
                .drawProb(probs.draw)
                .awayWinProb(probs.awayWin)
                .predictedHomeXg(predictedHomeXg)
                .predictedAwayXg(predictedAwayXg)
                .build();

        // STEP 7: Get AI analysis if requested
        String aiAnalysis = null;
        if (Boolean.TRUE.equals(request.getIncludeAiAnalysis())) {
            log.info("Requesting AI analysis from Gemini...");
            aiAnalysis = geminiAIService.generateMatchAnalysis(
                    homeTeam, awayTeam, homeForm, awayForm,
                    probs, predictedHomeXg, predictedAwayXg
            );
            prediction.setAiAnalysis(aiAnalysis);
        }

        // STEP 8: Calculate confidence level
        String confidence = calculateConfidence(probs);
        prediction.setConfidence(confidence);

        // STEP 9: Save prediction to database
        predictionRepository.save(prediction);
        log.info("Prediction saved to database with ID: {}", prediction.getId());

        // STEP 10: Build and return response
        return PredictionResponse.builder()
                .homeTeamName(homeTeam.getName())
                .awayTeamName(awayTeam.getName())
                .homeWinProbability(probs.homeWin)
                .drawProbability(probs.draw)
                .awayWinProbability(probs.awayWin)
                .predictedHomeXg(predictedHomeXg)
                .predictedAwayXg(predictedAwayXg)
                .aiAnalysis(aiAnalysis)
                .confidence(confidence)
                .build();
    }

    /**
     * Calculate team form from recent matches
     *
     * Form = How well has the team been playing recently?
     *
     * We look at:
     * - Points earned (3 for win, 1 for draw, 0 for loss)
     * - Goals scored
     * - Goals conceded
     * - Expected goals (xG)
     *
     * @param team The team
     * @param recentMatches Last 10 matches
     * @param isHome Will this team be playing at home?
     * @return TeamForm object with statistics
     */
    private TeamForm calculateTeamForm(Team team, List<Match> recentMatches, boolean isHome) {

        // If no recent matches, use team's season averages
        if (recentMatches.isEmpty()) {
            return new TeamForm(
                    team.getAverageXg() != null ? team.getAverageXg() : 1.5,
                    1.5,  // Default goals scored
                    1.5,  // Default goals conceded
                    0.0   // No form points
            );
        }

        // Limit to last 10 matches
        int limit = Math.min(10, recentMatches.size());
        List<Match> last10 = recentMatches.subList(0, limit);

        // Initialize counters
        double totalXg = 0.0;
        double totalGoalsScored = 0.0;
        double totalGoalsConceded = 0.0;
        double formPoints = 0.0;  // Win=3, Draw=1, Loss=0

        // Loop through each match and accumulate stats
        for (Match match : last10) {
            // Check if team was playing at home or away in this match
            boolean wasHome = match.getHomeTeam().getId().equals(team.getId());

            if (wasHome) {
                // Team was home in this match
                totalXg += match.getHomeXg() != null ? match.getHomeXg() : 0;
                totalGoalsScored += match.getHomeScore() != null ? match.getHomeScore() : 0;
                totalGoalsConceded += match.getAwayScore() != null ? match.getAwayScore() : 0;

                // Calculate points
                if (match.getHomeScore() != null && match.getAwayScore() != null) {
                    if (match.getHomeScore() > match.getAwayScore()) {
                        formPoints += 3;  // Win
                    } else if (match.getHomeScore().equals(match.getAwayScore())) {
                        formPoints += 1;  // Draw
                    }
                    // Loss = 0 points (no addition needed)
                }
            } else {
                // Team was away in this match
                totalXg += match.getAwayXg() != null ? match.getAwayXg() : 0;
                totalGoalsScored += match.getAwayScore() != null ? match.getAwayScore() : 0;
                totalGoalsConceded += match.getHomeScore() != null ? match.getHomeScore() : 0;

                // Calculate points
                if (match.getHomeScore() != null && match.getAwayScore() != null) {
                    if (match.getAwayScore() > match.getHomeScore()) {
                        formPoints += 3;  // Win
                    } else if (match.getAwayScore().equals(match.getHomeScore())) {
                        formPoints += 1;  // Draw
                    }
                }
            }
        }

        // Calculate averages
        double avgXg = totalXg / limit;
        double avgGoalsScored = totalGoalsScored / limit;
        double avgGoalsConceded = totalGoalsConceded / limit;

        return new TeamForm(avgXg, avgGoalsScored, avgGoalsConceded, formPoints);
    }

    /**
     * Calculate expected goals for this match
     *
     * Formula: xG = (baseXG × defensiveAdjustment × formMultiplier) + homeAdvantage
     *
     * Components:
     * - baseXG: Team's average attacking strength
     * - defensiveAdjustment: How weak is the opponent's defense?
     * - formMultiplier: Is the team in good form?
     * - homeAdvantage: Playing at home gives ~0.35 goals advantage
     *
     * @param attackingTeam Team that's attacking
     * @param defendingTeam Team that's defending
     * @param isHome Is attacking team playing at home?
     * @return Predicted expected goals
     */
    private double calculateExpectedGoals(
            TeamForm attackingTeam,
            TeamForm defendingTeam,
            boolean isHome) {

        // Base xG: How good is this team at creating chances?
        double baseXg = attackingTeam.avgXg;

        // Defensive adjustment: How bad is opponent's defense?
        // If opponent concedes 2.0 goals/game (league avg is 1.5)
        // Then: 2.0 / 1.5 = 1.33 (easier to score against them)
        double leagueAverageGoalsConceded = 1.5;
        double defensiveAdjustment = defendingTeam.avgGoalsConceded / leagueAverageGoalsConceded;

        // Home advantage: Statistically worth about 0.35 goals
        double homeAdvantage = isHome ? 0.35 : 0.0;

        // Form multiplier: Is team in good form?
        // Good form (24 points) vs average (15 points) = boost
        // Bad form (6 points) vs average (15 points) = penalty
        double averageFormPoints = 15.0;  // 5 wins out of 10 matches
        double formMultiplier = 1.0 + ((attackingTeam.formPoints - averageFormPoints) / 30.0);

        // Final calculation
        double xG = (baseXg * defensiveAdjustment * formMultiplier) + homeAdvantage;

        // Keep xG realistic (between 0.5 and 3.5 goals)
        return Math.max(0.5, Math.min(3.5, xG));
    }

    /**
     * Calculate match probabilities using Poisson distribution
     *
     * This is the MATHEMATICAL CORE of our prediction! 📊
     *
     * Poisson Distribution:
     * - Statistical model for predicting rare events
     * - Perfect for football goals (discrete events in time)
     * - Answers: "If team averages 2.5 goals, what's probability of exactly 3 goals?"
     *
     * We check ALL possible scorelines (0-0, 1-0, 1-1, 2-0, 2-1, 2-2, etc.)
     * and sum up probabilities for:
     * - Home win (home score > away score)
     * - Draw (home score = away score)
     * - Away win (away score > home score)
     *
     * @param homeXg Expected goals for home team
     * @param awayXg Expected goals for away team
     * @return MatchProbabilities object with win/draw/loss percentages
     */
    private MatchProbabilities calculateMatchProbabilities(double homeXg, double awayXg) {

        double homeWin = 0.0;
        double draw = 0.0;
        double awayWin = 0.0;

        // Check all possible scorelines from 0-0 to 5-5
        // (Going beyond 5 goals is extremely rare, doesn't add much precision)
        for (int homeGoals = 0; homeGoals <= 5; homeGoals++) {
            for (int awayGoals = 0; awayGoals <= 5; awayGoals++) {

                // Calculate probability of THIS EXACT scoreline
                // Example: What's probability of exactly 2-1?
                double homeProbability = poissonProbability(homeGoals, homeXg);
                double awayProbability = poissonProbability(awayGoals, awayXg);
                double combinedProbability = homeProbability * awayProbability;

                // Add to appropriate outcome
                if (homeGoals > awayGoals) {
                    homeWin += combinedProbability;  // 1-0, 2-0, 2-1, 3-1, etc.
                } else if (homeGoals == awayGoals) {
                    draw += combinedProbability;     // 0-0, 1-1, 2-2, etc.
                } else {
                    awayWin += combinedProbability;  // 0-1, 0-2, 1-2, etc.
                }
            }
        }

        // Normalize to ensure probabilities sum to 1.0 (100%)
        double total = homeWin + draw + awayWin;

        return new MatchProbabilities(
                homeWin / total,   // e.g., 0.523 = 52.3%
                draw / total,      // e.g., 0.267 = 26.7%
                awayWin / total    // e.g., 0.210 = 21.0%
        );
    }

    /**
     * Poisson Probability Function
     *
     * Calculates: P(X = k) = (λ^k × e^(-λ)) / k!
     *
     * Where:
     * - k = number of goals we're checking for (0, 1, 2, 3, etc.)
     * - λ (lambda) = expected number of goals (xG)
     * - e = Euler's number (2.71828...)
     * - k! = k factorial (e.g., 3! = 3×2×1 = 6)
     *
     * Example: If team's xG is 2.5, what's probability of exactly 3 goals?
     * P(X=3) = (2.5^3 × e^(-2.5)) / 3! = 0.2138 = 21.38%
     *
     * @param k Number of goals
     * @param lambda Expected goals (xG)
     * @return Probability (0 to 1)
     */
    private double poissonProbability(int k, double lambda) {
        // Formula: (λ^k × e^(-λ)) / k!
        return (Math.pow(lambda, k) * Math.exp(-lambda)) / factorial(k);
    }

    /**
     * Calculate factorial: n! = n × (n-1) × (n-2) × ... × 1
     *
     * Examples:
     * 0! = 1
     * 1! = 1
     * 3! = 3 × 2 × 1 = 6
     * 5! = 5 × 4 × 3 × 2 × 1 = 120
     */
    private long factorial(int n) {
        if (n <= 1) return 1;
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * Calculate confidence level in prediction
     *
     * HIGH: One outcome is very likely (>60%)
     * MEDIUM: Clear favorite but not dominant (45-60%)
     * LOW: Very close match, all outcomes possible (<45%)
     */
    private String calculateConfidence(MatchProbabilities probs) {
        double maxProb = Math.max(probs.homeWin, Math.max(probs.draw, probs.awayWin));

        if (maxProb > 0.60) {
            return "HIGH";
        } else if (maxProb > 0.45) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Get all predictions (for admin/analyst users)
     */
    public List<Prediction> getAllPredictions() {
        return predictionRepository.findAll();
    }

    /**
     * Get prediction by ID
     */
    public Prediction getPredictionById(Long id) {
        return predictionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prediction not found with ID: " + id));
    }

    /**
     * Get recent predictions
     */
    public List<Prediction> getRecentPredictions() {
        return predictionRepository.findTop10ByOrderByCreatedAtDesc();
    }
}