package com.cricket.match.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root for a single match. Deliberately does NOT hold full Player
 * or Team objects — this service only stores playerId/teamId references.
 * Player details live in the Player Service; keeping the boundary strict is
 * what makes this a real microservice instead of a distributed monolith.
 */
@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String team1;

    @Column(nullable = false)
    private String team2;

    private String venue;

    @Column(name = "match_date", nullable = false)
    private LocalDateTime matchDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchFormat format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MatchStatus status = MatchStatus.SCHEDULED;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Ball> balls = new ArrayList<>();

    public void addBall(Ball ball) {
        ball.setMatch(this);
        this.balls.add(ball);
        if (this.status == MatchStatus.SCHEDULED) {
            this.status = MatchStatus.LIVE;
        }
    }
}
