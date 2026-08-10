package com.cricket.match.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A single delivery. bowlerId / batsmanId are references into the Player
 * Service, not embedded entities — this keeps the two services independently
 * deployable and independently owned.
 */
@Entity
@Table(name = "balls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ball {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "innings_number", nullable = false)
    private int inningsNumber;

    @Column(name = "over_number", nullable = false)
    private int overNumber;

    @Column(name = "ball_number", nullable = false)
    private int ballNumber;

    @Column(name = "bowler_id", nullable = false)
    private UUID bowlerId;

    @Column(name = "batsman_id", nullable = false)
    private UUID batsmanId;

    @Column(nullable = false)
    @Builder.Default
    private int runs = 0;

    @Column(name = "is_wicket", nullable = false)
    @Builder.Default
    private boolean wicket = false;

    @Column(name = "is_wide", nullable = false)
    @Builder.Default
    private boolean wide = false;

    @Column(name = "is_no_ball", nullable = false)
    @Builder.Default
    private boolean noBall = false;

    @Column(nullable = false)
    @Builder.Default
    private int extras = 0;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
