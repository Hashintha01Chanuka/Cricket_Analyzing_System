package com.cricket.match.repository;

import com.cricket.match.entity.Ball;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BallRepository extends JpaRepository<Ball, UUID> {

    @Query("SELECT b FROM Ball b WHERE b.match.id = :matchId AND b.inningsNumber = :innings " +
           "ORDER BY b.overNumber ASC, b.ballNumber ASC")
    List<Ball> findByMatchAndInningsOrdered(@Param("matchId") UUID matchId,
                                             @Param("innings") int innings);

    @Query("SELECT b FROM Ball b WHERE b.match.id = :matchId AND b.inningsNumber = :innings " +
            "AND b.batsmanId = :batsmanId ORDER BY b.overNumber ASC, b.ballNumber ASC")
    List<Ball> findByMatchAndInningsAndBatsmanOrdered(@Param("matchId") UUID matchId,
                                                      @Param("innings") int innings,
                                                      @Param("batsmanId") UUID batsmanId);

    @Query("SELECT b FROM Ball b WHERE b.match.id = :matchId AND b.inningsNumber = :innings " +
            "AND b.bowlerId = :bowlerId ORDER BY b.overNumber ASC, b.ballNumber ASC")
    List<Ball> findByMatchAndInningsAndBowlerOrdered(@Param("matchId") UUID matchId,
                                                     @Param("innings") int innings,
                                                     @Param("bowlerId") UUID bowlerId);
}
