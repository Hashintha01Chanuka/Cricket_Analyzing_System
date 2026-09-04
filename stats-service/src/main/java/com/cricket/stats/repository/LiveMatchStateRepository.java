package com.cricket.stats.repository;

import com.cricket.stats.entity.LiveMatchState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LiveMatchStateRepository extends JpaRepository<LiveMatchState, UUID> {

    Optional<LiveMatchState> findByMatchIdAndInningsNumber(UUID matchId, int inningsNumber);
}