package com.cricket.stats.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stats Service doesn't own match or player data — those belong to Match
 * Service and Player Service. This is the one thing it genuinely owns:
 * completed-match results, recorded here specifically to power head-to-head
 * queries without re-fetching and re-deriving them from Match Service on
 * every request. Right now it's populated by a manual POST after a match
 * ends; in Phase 3 this becomes a Kafka consumer reacting to a
 * "MatchCompleted" event instead.
 */
@Entity
@Table(name = "match_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResult {

    @Id
    @GeneratedValue
    private UUID id;

    /** References the Match Service's match id — not a foreign key across services. */
    @Column(name = "match_id")
    private UUID matchId;

    @Column(nullable = false)
    private String team1;

    @Column(nullable = false)
    private String team2;

    @Column(nullable = false)
    private String winner;

    private String margin;

    @Column(name = "match_date", nullable = false)
    private LocalDate matchDate;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime recordedAt = LocalDateTime.now();
}
