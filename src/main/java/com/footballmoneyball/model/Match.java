package com.footballmoneyball.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)  // Changed from LAZY to EAGER
    @JoinColumn(name = "home_team_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.EAGER)  // Changed from LAZY to EAGER
    @JoinColumn(name = "away_team_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Team awayTeam;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Column(name = "home_xg")
    private Double homeXg;

    @Column(name = "away_xg")
    private Double awayXg;

    @Column(name = "home_xa")
    private Double homeXa;

    @Column(name = "away_xa")
    private Double awayXa;

    @Column(name = "home_possession")
    private Double homePossession;

    @Column(name = "away_possession")
    private Double awayPossession;

    @Column(name = "home_shots")
    private Integer homeShots;

    @Column(name = "away_shots")
    private Integer awayShots;

    @Column(name = "match_date", nullable = false)
    private LocalDateTime matchDate;

    @Column(name = "completed")
    private Boolean completed = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}