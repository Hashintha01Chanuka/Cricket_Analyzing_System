package com.cricket.player.repository;

import com.cricket.player.entity.CareerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CareerStatsRepository extends JpaRepository<CareerStats, UUID> {

    @Query("SELECT c FROM CareerStats c WHERE c.player.id = :playerId")
    Optional<CareerStats> findByPlayerId(@Param("playerId") UUID playerId);

    @Query("SELECT c FROM CareerStats c JOIN FETCH c.player")
    List<CareerStats> findAllWithPlayer();
}
