package com.footballmoneyball.util;

import com.footballmoneyball.dto.BettingMarkets.MarketOdds;

/**
 * Converts probabilities to various odds formats
 */
public class OddsConverter {

    /**
     * Convert probability to all odds formats
     */
    public static MarketOdds convertProbability(double probability) {
        if (probability <= 0 || probability > 1) {
            throw new IllegalArgumentException("Probability must be between 0 and 1");
        }

        double percentage = probability * 100;
        double decimalOdds = 1.0 / probability;
        String fractionalOdds = toFractional(decimalOdds);
        String americanOdds = toAmerican(decimalOdds);

        return MarketOdds.builder()
                .probability(probability)
                .percentage(Math.round(percentage * 10) / 10.0)
                .decimalOdds(Math.round(decimalOdds * 100) / 100.0)
                .fractionalOdds(fractionalOdds)
                .americanOdds(americanOdds)
                .build();
    }

    /**
     * Convert decimal odds to fractional (e.g., 1.91 -> "10/11")
     */
    private static String toFractional(double decimalOdds) {
        double fractional = decimalOdds - 1;

        // Find best fraction approximation
        int numerator = 1;
        int denominator = 1;
        double minError = Double.MAX_VALUE;

        for (int d = 1; d <= 100; d++) {
            int n = (int) Math.round(fractional * d);
            double error = Math.abs((double) n / d - fractional);

            if (error < minError) {
                minError = error;
                numerator = n;
                denominator = d;
            }

            if (error < 0.01) break; // Good enough
        }

        return numerator + "/" + denominator;
    }

    /**
     * Convert decimal odds to American (e.g., 1.91 -> "-110")
     */
    private static String toAmerican(double decimalOdds) {
        if (decimalOdds >= 2.0) {
            // Underdog (positive)
            int american = (int) Math.round((decimalOdds - 1) * 100);
            return "+" + american;
        } else {
            // Favorite (negative)
            int american = (int) Math.round(-100 / (decimalOdds - 1));
            return String.valueOf(american);
        }
    }
}