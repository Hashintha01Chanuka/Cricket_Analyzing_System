package com.cricket.match.dto;

import com.cricket.match.entity.Match;
import com.cricket.match.entity.MatchFormat;
import com.cricket.match.entity.MatchStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MatchResponse(
        UUID id,
        String team1,
        String team2,
        String venue,
        LocalDateTime matchDate,
        MatchFormat format,
        MatchStatus status,
        LocalDateTime createdAt
) {
    public static MatchResponse from(Match match) {
        return new MatchResponse(
                match.getId(),
                match.getTeam1(),
                match.getTeam2(),
                match.getVenue(),
                match.getMatchDate(),
                match.getFormat(),
                match.getStatus(),
                match.getCreatedAt()
        );
    }
}
