package com.footballmoneyball.controller;

import com.footballmoneyball.dto.PredictionRequest;
import com.footballmoneyball.dto.PredictionResponse;
import com.footballmoneyball.model.Prediction;
import com.footballmoneyball.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Prediction Controller
 *
 * THE MAIN EVENT! 🎯
 *
 * Handles match prediction endpoints:
 * - POST /api/predictions/predict - Make a prediction
 * - GET /api/predictions - Get all predictions
 * - GET /api/predictions/{id} - Get prediction by ID
 * - GET /api/predictions/recent - Get recent predictions
 */
@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PredictionController {

    private final PredictionService predictionService;

    /**
     * Make a match prediction
     *
     * POST /api/predictions/predict
     * Body: {
     *   "homeTeamId": 1,
     *   "awayTeamId": 2,
     *   "includeAiAnalysis": true
     * }
     *
     * Returns: {
     *   "homeTeamName": "Manchester City",
     *   "awayTeamName": "Arsenal",
     *   "homeWinProbability": 0.523,
     *   "drawProbability": 0.267,
     *   "awayWinProbability": 0.210,
     *   "predictedHomeXg": 2.74,
     *   "predictedAwayXg": 1.77,
     *   "aiAnalysis": "Manchester City holds...",
     *   "confidence": "MEDIUM"
     * }
     *
     * Anyone can use (ADMIN, ANALYST, GUEST)
     */
    @PostMapping("/predict")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST', 'GUEST')")
    public ResponseEntity<PredictionResponse> predictMatch(@RequestBody PredictionRequest request) {
        log.info("Prediction request: Team {} vs Team {}",
                request.getHomeTeamId(), request.getAwayTeamId());

        PredictionResponse response = predictionService.predictMatch(request);

        log.info("Prediction complete: {} - {} vs {} - {}",
                response.getHomeWinProbability(), response.getHomeTeamName(),
                response.getAwayWinProbability(), response.getAwayTeamName());

        return ResponseEntity.ok(response);
    }

    /**
     * Get all predictions
     *
     * GET /api/predictions
     * Returns: List of all predictions
     *
     * ADMIN and ANALYST only
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST')")
    public ResponseEntity<List<Prediction>> getAllPredictions() {
        log.info("Fetching all predictions");
        List<Prediction> predictions = predictionService.getAllPredictions();
        return ResponseEntity.ok(predictions);
    }

    /**
     * Get prediction by ID
     *
     * GET /api/predictions/1
     * Returns: Single prediction
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST', 'GUEST')")
    public ResponseEntity<Prediction> getPredictionById(@PathVariable Long id) {
        log.info("Fetching prediction with ID: {}", id);
        Prediction prediction = predictionService.getPredictionById(id);
        return ResponseEntity.ok(prediction);
    }

    /**
     * Get recent predictions
     *
     * GET /api/predictions/recent
     * Returns: Last 10 predictions
     */
    @GetMapping("/recent")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ANALYST', 'GUEST')")
    public ResponseEntity<List<Prediction>> getRecentPredictions() {
        log.info("Fetching recent predictions");
        List<Prediction> predictions = predictionService.getRecentPredictions();
        return ResponseEntity.ok(predictions);
    }
}