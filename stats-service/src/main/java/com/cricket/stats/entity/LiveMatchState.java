package com.cricket.stats.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * This is what the synchronous REST call to Match Service (MatchServiceClient)
 * is being replaced by: instead of asking Match Service "what's the current
 * run rate?" on every request, Stats Service now maintains its own
 * continuously-updated copy of the essentials (runs, balls, wickets) by
 * consuming ball events as they happen. Reading this table is fast and has
 * zero runtime dependency on Match Service being up. This is the standard
 * "eventual consistency instead of synchronous coupling" tradeoff that
 * event-driven architecture buys you — worth naming directly in an
 * interview as the reason this table exists.
 */
@Entity
@Table(name = "live_match_state", uniqueConstraints = @UniqueConstraint(columnNames = {"match_id", "innings_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveMatchState {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "match_id", nullable = false)
    private UUID matchId;

    @Column(name = "innings_number", nullable = false)
    private int inningsNumber;

    @Column(name = "total_runs", nullable = false)
    @Builder.Default
    private int totalRuns = 0;

    /** Legal deliveries only — wides and no-balls don't advance this. */
    @Column(name = "legal_balls", nullable = false)
    @Builder.Default
    private int legalBalls = 0;

    @Column(name = "wickets_lost", nullable = false)
    @Builder.Default
    private int wicketsLost = 0;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public double getOversBowled() {
        return legalBalls / 6.0;
    }

    public double getCurrentRunRate() {
        double overs = getOversBowled();
        return overs == 0 ? 0.0 : Math.round((totalRuns / overs) * 100.0) / 100.0;
    }
}