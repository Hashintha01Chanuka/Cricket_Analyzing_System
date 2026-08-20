package com.cricket.player.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Aggregate career numbers for a player. MVP scope keeps this format-agnostic
 * (one row per player, not per Test/ODI/T20) — splitting stats by format is a
 * reasonable "if I had more time" extension to mention in an interview, but
 * isn't needed to demonstrate the architecture.
 */
@Entity
@Table(name = "career_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareerStats {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private Player player;

    @Builder.Default
    private int matches = 0;

    @Builder.Default
    private int innings = 0;

    @Column(name = "runs_scored")
    @Builder.Default
    private int runsScored = 0;

    @Column(name = "balls_faced")
    @Builder.Default
    private int ballsFaced = 0;

    @Column(name = "highest_score")
    @Builder.Default
    private int highestScore = 0;

    @Column(name = "centuries")
    @Builder.Default
    private int centuries = 0;

    @Column(name = "fifties")
    @Builder.Default
    private int fifties = 0;

    @Column(name = "wickets_taken")
    @Builder.Default
    private int wicketsTaken = 0;

    @Column(name = "runs_conceded")
    @Builder.Default
    private int runsConceded = 0;

    @Column(name = "balls_bowled")
    @Builder.Default
    private int ballsBowled = 0;

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // --- Derived metrics, computed on read rather than stored, so they can
    // never drift out of sync with the raw counters above. ---

    @Transient
    public double getBattingAverage() {
        // Simplified: proper batting average divides by dismissals, not
        // innings (not-outs should be excluded). We don't track not-outs
        // separately in the MVP, so this treats every innings as a
        // dismissal — a known, documented simplification (see README).
        return innings == 0 ? 0.0 : round(runsScored / (double) innings);
    }

    @Transient
    public double getBattingStrikeRate() {
        return ballsFaced == 0 ? 0.0 : round((runsScored * 100.0) / ballsFaced);
    }

    @Transient
    public double getBowlingEconomy() {
        double overs = ballsBowled / 6.0;
        return overs == 0 ? 0.0 : round(runsConceded / overs);
    }

    @Transient
    public double getBowlingAverage() {
        return wicketsTaken == 0 ? 0.0 : round(runsConceded / (double) wicketsTaken);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
