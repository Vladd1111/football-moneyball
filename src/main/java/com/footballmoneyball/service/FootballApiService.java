package com.footballmoneyball.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.footballmoneyball.model.Match;
import com.footballmoneyball.model.Team;
import com.footballmoneyball.repository.MatchRepository;
import com.footballmoneyball.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Football API Service
 *
 * Integrates with free-api-live-football-data (RapidAPI) to fetch:
 * - Real fixtures by league
 * - Team statistics
 * - Live match data
 *
 * FREE tier: 100 requests/day
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FootballApiService {

    @Value("${rapidapi.key}")
    private String apiKey;

    private final WebClient.Builder webClientBuilder;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;

    private static final String API_BASE_URL = "https://free-api-live-football-data.p.rapidapi.com";
    private static final String API_HOST     = "free-api-live-football-data.p.rapidapi.com";
    private static final int    LEAGUE_ID    = 42;

    /**
     * Fetch upcoming fixtures for the configured league
     */
    public List<Match> fetchUpcomingFixtures() {
        log.info("Fetching fixtures from free-api-live-football-data for leagueId={}", LEAGUE_ID);

        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(API_BASE_URL)
                    .defaultHeader("X-RapidAPI-Key", apiKey)
                    .defaultHeader("X-RapidAPI-Host", API_HOST)
                    .build();

            String response = webClient.get()
                    .uri("/football-get-all-matches-by-league?leagueid=" + LEAGUE_ID)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseFixtures(response);

        } catch (Exception e) {
            log.error("Failed to fetch fixtures: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch fixtures from API");
        }
    }

    /**
     * Fetch team statistics for a team
     */
    public void updateTeamStatistics(Long teamId, int apiFootballTeamId) {
        log.info("Fetching team statistics for API team ID: {}", apiFootballTeamId);

        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(API_BASE_URL)
                    .defaultHeader("X-RapidAPI-Key", apiKey)
                    .defaultHeader("X-RapidAPI-Host", API_HOST)
                    .build();

            String response = webClient.get()
                    .uri("/football-get-team-statistics?teamid=" + apiFootballTeamId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            updateTeamFromStats(teamId, response);

        } catch (Exception e) {
            log.error("Failed to fetch team stats: {}", e.getMessage(), e);
        }
    }

    /**
     * Parse fixtures response and save to database
     */
    private List<Match> parseFixtures(String jsonResponse) {
        List<Match> matches = new ArrayList<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            JsonNode fixtures = root.path("response");

            for (JsonNode fixture : fixtures) {
                // Get team IDs from API
                int homeTeamApiId = fixture.path("teams").path("home").path("id").asInt();
                int awayTeamApiId = fixture.path("teams").path("away").path("id").asInt();

                // Get or create teams in our database
                Team homeTeam = getOrCreateTeam(
                        fixture.path("teams").path("home").path("name").asText(),
                        homeTeamApiId
                );
                Team awayTeam = getOrCreateTeam(
                        fixture.path("teams").path("away").path("name").asText(),
                        awayTeamApiId
                );

                // Parse match date
                String dateStr = fixture.path("fixture").path("date").asText();
                LocalDateTime matchDate = LocalDateTime.parse(dateStr,
                        DateTimeFormatter.ISO_DATE_TIME);

                // Check if match already exists
                boolean exists = matchRepository.existsByHomeTeamAndAwayTeamAndMatchDate(
                        homeTeam, awayTeam, matchDate
                );

                if (!exists) {
                    Match match = Match.builder()
                            .homeTeam(homeTeam)
                            .awayTeam(awayTeam)
                            .matchDate(matchDate)
                            .completed(false)
                            .build();

                    matches.add(matchRepository.save(match));
                    log.info("Saved fixture: {} vs {} on {}",
                            homeTeam.getName(), awayTeam.getName(), matchDate);
                }
            }

        } catch (Exception e) {
            log.error("Error parsing fixtures: {}", e.getMessage(), e);
        }

        return matches;
    }

    /**
     * Get team from database or create new one
     */
    private Team getOrCreateTeam(String teamName, int apiTeamId) {
        return teamRepository.findByName(teamName)
                .orElseGet(() -> {
                    Team team = Team.builder()
                            .name(teamName)
                            .league("Premier League")
                            .wins(0)
                            .draws(0)
                            .losses(0)
                            .goalsScored(0)
                            .goalsConceded(0)
                            .averageXg(1.5)
                            .averageXa(1.2)
                            .build();

                    Team saved = teamRepository.save(team);
                    log.info("Created new team: {}", teamName);

                    // Fetch real statistics
                    updateTeamStatistics(saved.getId(), apiTeamId);

                    return saved;
                });
    }

    /**
     * Update team with real statistics from API
     */
    private void updateTeamFromStats(Long teamId, String jsonResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            JsonNode stats = root.path("response");

            if (stats.isMissingNode()) {
                log.warn("No statistics found in response");
                return;
            }

            // Extract statistics
            JsonNode fixtures = stats.path("fixtures");
            int wins = fixtures.path("wins").path("total").asInt(0);
            int draws = fixtures.path("draws").path("total").asInt(0);
            int losses = fixtures.path("losses").path("total").asInt(0);

            JsonNode goals = stats.path("goals");
            int goalsScored = goals.path("for").path("total").path("total").asInt(0);
            int goalsConceded = goals.path("against").path("total").path("total").asInt(0);

            int played = wins + draws + losses;
            double avgXg = played > 0 ? (double) goalsScored / played : 1.5;
            double avgXa = avgXg * 0.8;

            // Update team
            Team team = teamRepository.findById(teamId).orElse(null);
            if (team != null) {
                team.setWins(wins);
                team.setDraws(draws);
                team.setLosses(losses);
                team.setGoalsScored(goalsScored);
                team.setGoalsConceded(goalsConceded);
                team.setAverageXg(avgXg);
                team.setAverageXa(avgXa);

                teamRepository.save(team);
                log.info("Updated team statistics for: {}", team.getName());
            }

        } catch (Exception e) {
            log.error("Error updating team stats: {}", e.getMessage(), e);
        }
    }
}