package com.footballmoneyball.service;

import com.footballmoneyball.model.Team;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Google Gemini AI Service
 *
 * Integrates with Google's Gemini API to generate AI-powered match analysis.
 *
 * FREE: 1,500 requests per day
 * Get your API key: https://makersuite.google.com/app/apikey
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAIService {

    // Inject API key from application.properties
    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient.Builder webClientBuilder;

    // Gemini API endpoint
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    /**
     * Generate AI-powered match analysis
     *
     * Takes prediction data and asks Gemini to provide expert commentary.
     *
     * @return AI-generated analysis (3-4 sentences)
     */
    public String generateMatchAnalysis(
            Team homeTeam,
            Team awayTeam,
            TeamForm homeForm,
            TeamForm awayForm,
            MatchProbabilities probs,
            double homeXg,
            double awayXg) {

        // Build prompt with all match data
        String prompt = buildAnalysisPrompt(
                homeTeam, awayTeam, homeForm, awayForm, probs, homeXg, awayXg
        );

        try {
            // Create request body for Gemini API
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.7,
                            "maxOutputTokens", 2048,          // Increased further
                            "topP", 0.95,
                            "topK", 40
                    ),
                    "safetySettings", List.of(
                            Map.of("category", "HARM_CATEGORY_HARASSMENT", "threshold", "BLOCK_NONE"),
                            Map.of("category", "HARM_CATEGORY_HATE_SPEECH", "threshold", "BLOCK_NONE"),
                            Map.of("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold", "BLOCK_NONE"),
                            Map.of("category", "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold", "BLOCK_NONE")
                    )
            );

            // Build WebClient
            // Note: API key goes in URL as query parameter
            WebClient webClient = webClientBuilder
                    .baseUrl(GEMINI_API_URL + "?key=" + apiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            log.info("Calling Gemini API for match: {} vs {}",
                    homeTeam.getName(), awayTeam.getName());

            // Make HTTP POST request
            Mono<Map> responseMono = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class);

            // Wait for response (blocking call)
            Map response = responseMono.block();

            // Parse Gemini's response
            // Structure: { candidates: [{ content: { parts: [{ text: "..." }] } }] }
            if (response != null && response.containsKey("candidates")) {
                @SuppressWarnings("unchecked")
                List<Map> candidates = (List<Map>) response.get("candidates");

                if (!candidates.isEmpty()) {
                    Map firstCandidate = candidates.get(0);
                    @SuppressWarnings("unchecked")
                    Map content = (Map) firstCandidate.get("content");

                    if (content != null && content.containsKey("parts")) {
                        @SuppressWarnings("unchecked")
                        List<Map> parts = (List<Map>) content.get("parts");

                        if (!parts.isEmpty()) {
                            Map firstPart = parts.get(0);
                            String analysisText = (String) firstPart.get("text");

                            log.info("Successfully received Gemini analysis");
                            return analysisText;
                        }
                    }
                }
            }

            log.warn("Gemini API returned unexpected response structure");
            return "AI analysis unavailable at this time.";

        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            return "AI analysis could not be generated due to a technical error.";
        }
    }

    /**
     * Build detailed prompt for Gemini
     */
    private String buildAnalysisPrompt(
            Team homeTeam,
            Team awayTeam,
            TeamForm homeForm,
            TeamForm awayForm,
            MatchProbabilities probs,
            double homeXg,
            double awayXg) {

        return String.format(
                "You are a football analyst. Write EXACTLY 4 complete sentences analyzing this match. " +
                        "Do NOT stop mid-sentence.\n\n" +
                        "%s (Home, Form: %.1f pts, xG: %.2f) vs %s (Away, Form: %.1f pts, xG: %.2f)\n" +
                        "Prediction: Home %.1f%%, Draw %.1f%%, Away %.1f%%\n" +
                        "Expected: %.2f-%.2f\n\n" +
                        "Write 4 complete sentences covering: 1) main advantage, 2) key stats, 3) risks, 4) likely outcome.",
                homeTeam.getName(),
                homeForm.formPoints,
                homeForm.avgXg,
                awayTeam.getName(),
                awayForm.formPoints,
                awayForm.avgXg,
                probs.homeWin * 100,
                probs.draw * 100,
                probs.awayWin * 100,
                homeXg,
                awayXg
        );
    }

    // Inner classes for data structures

    public static class TeamForm {
        public double avgXg;
        public double avgGoalsScored;
        public double avgGoalsConceded;
        public double formPoints;

        public TeamForm(double avgXg, double avgGoalsScored,
                        double avgGoalsConceded, double formPoints) {
            this.avgXg = avgXg;
            this.avgGoalsScored = avgGoalsScored;
            this.avgGoalsConceded = avgGoalsConceded;
            this.formPoints = formPoints;
        }
    }

    public static class MatchProbabilities {
        public double homeWin;
        public double draw;
        public double awayWin;

        public MatchProbabilities(double homeWin, double draw, double awayWin) {
            this.homeWin = homeWin;
            this.draw = draw;
            this.awayWin = awayWin;
        }
    }
}