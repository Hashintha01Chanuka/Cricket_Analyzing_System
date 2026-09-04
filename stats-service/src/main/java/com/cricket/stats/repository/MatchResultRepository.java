package com.cricket.stats.repository;

import com.cricket.stats.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {

    @Query("SELECT m FROM MatchResult m WHERE " +
           "(LOWER(m.team1) = LOWER(:teamA) AND LOWER(m.team2) = LOWER(:teamB)) OR " +
           "(LOWER(m.team1) = LOWER(:teamB) AND LOWER(m.team2) = LOWER(:teamA)) " +
           "ORDER BY m.matchDate DESC")
    List<MatchResult> findHeadToHead(@Param("teamA") String teamA, @Param("teamB") String teamB);
}
