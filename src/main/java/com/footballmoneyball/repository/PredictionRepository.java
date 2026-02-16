package com.footballmoneyball.repository;

import com.footballmoneyball.model.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Prediction Repository
 *
 * Provides database access methods for Prediction entity.
 * Used for storing and retrieving match predictions.
 */
@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    /**
     * Find all predictions for a specific matchup
     * Useful for seeing prediction history for recurring matches
     */
    List<Prediction> findByHomeTeamIdAndAwayTeamId(Long homeTeamId, Long awayTeamId);

    /**
     * Find all predictions involving a specific team (home or away)
     */
    @Query("SELECT p FROM Prediction p WHERE p.homeTeamId = :teamId OR p.awayTeamId = :teamId ORDER BY p.createdAt DESC")
    List<Prediction> findByTeamId(@Param("teamId") Long teamId);

    /**
     * Get recent predictions (last N predictions)
     * Ordered by most recent first
     */
    List<Prediction> findTop10ByOrderByCreatedAtDesc();

    /**
     * Find predictions with high confidence
     */
    List<Prediction> findByConfidence(String confidence);
}