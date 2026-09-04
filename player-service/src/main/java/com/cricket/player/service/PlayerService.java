package com.cricket.player.service;

import com.cricket.player.dto.*;
import com.cricket.player.entity.CareerStats;
import com.cricket.player.entity.Player;
import com.cricket.player.exception.PlayerNotFoundException;
import com.cricket.player.repository.CareerStatsRepository;
import com.cricket.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.ToIntFunction;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final CareerStatsRepository careerStatsRepository;

    @Transactional
    public PlayerResponse createPlayer(PlayerRequest request) {
        Player player = Player.builder()
                .name(request.name())
                .country(request.country())
                .role(request.role())
                .battingStyle(request.battingStyle())
                .bowlingStyle(request.bowlingStyle())
                .dateOfBirth(request.dateOfBirth())
                .build();
        player = playerRepository.save(player);

        // Every player gets a career stats row created up front, at all
        // zeroes, so downstream services can always assume it exists rather
        // than having to handle "stats row missing" as a separate case.
        CareerStats stats = CareerStats.builder().player(player).build();
        careerStatsRepository.save(stats);

        return PlayerResponse.from(player);
    }

    @Transactional(readOnly = true)
    public PlayerResponse getPlayer(UUID playerId) {
        return PlayerResponse.from(findPlayerOrThrow(playerId));
    }

    @Transactional(readOnly = true)
    public List<PlayerResponse> listPlayers() {
        return playerRepository.findAll().stream().map(PlayerResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CareerStatsResponse getCareerStats(UUID playerId) {
        findPlayerOrThrow(playerId);
        CareerStats stats = careerStatsRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
        return CareerStatsResponse.from(stats);
    }

    /**
     * Applies one innings/match performance as a delta onto the player's
     * running totals. Called after each match — in a fuller build this
     * would be triggered by a Kafka event from Match Service rather than
     * called directly.
     */
    @Transactional
    public CareerStatsResponse updateCareerStats(UUID playerId, CareerStatsUpdateRequest request) {
        findPlayerOrThrow(playerId);
        CareerStats stats = careerStatsRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));

        boolean batted = request.runsScored() > 0 || request.ballsFaced() > 0;
        boolean bowled = request.ballsBowled() > 0;

        if (batted) {
            stats.setInnings(stats.getInnings() + 1);
            stats.setRunsScored(stats.getRunsScored() + request.runsScored());
            stats.setBallsFaced(stats.getBallsFaced() + request.ballsFaced());
            stats.setHighestScore(Math.max(stats.getHighestScore(), request.runsScored()));
            if (request.runsScored() >= 100) {
                stats.setCenturies(stats.getCenturies() + 1);
            } else if (request.runsScored() >= 50) {
                stats.setFifties(stats.getFifties() + 1);
            }
        }

        if (bowled) {
            stats.setWicketsTaken(stats.getWicketsTaken() + request.wicketsTaken());
            stats.setRunsConceded(stats.getRunsConceded() + request.runsConceded());
            stats.setBallsBowled(stats.getBallsBowled() + request.ballsBowled());
        }

        if (batted || bowled) {
            stats.setMatches(stats.getMatches() + 1);
        }

        return CareerStatsResponse.from(careerStatsRepository.save(stats));
    }

    /**
     * Top-K leaderboard using a bounded min-heap instead of sorting the
     * whole player list. For n players and a top-K request, this is
     * O(n log k) rather than the O(n log n) a full sort would cost — the
     * gap matters once n is large and k stays small (e.g. "top 10 out of
     * 5000 players"). We push every candidate, and whenever the heap grows
     * past size K we pop the smallest, so only the K largest survive.
     */
    @Transactional(readOnly = true)
    public List<LeaderboardEntry> topByRuns(int limit) {
        return topK(limit, CareerStats::getRunsScored);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> topByWickets(int limit) {
        return topK(limit, CareerStats::getWicketsTaken);
    }

    private List<LeaderboardEntry> topK(int limit, ToIntFunction<CareerStats> metric) {
        List<CareerStats> all = careerStatsRepository.findAllWithPlayer();

        PriorityQueue<CareerStats> minHeap = new PriorityQueue<>(
                limit, Comparator.comparingInt(metric::applyAsInt));

        for (CareerStats stats : all) {
            minHeap.offer(stats);
            if (minHeap.size() > limit) {
                minHeap.poll(); // evict the current smallest
            }
        }

        return minHeap.stream()
                .sorted(Comparator.comparingInt(metric::applyAsInt).reversed())
                .map(s -> new LeaderboardEntry(s.getPlayer().getId(), s.getPlayer().getName(), metric.applyAsInt(s)))
                .toList();
    }

    private Player findPlayerOrThrow(UUID playerId) {
        return playerRepository.findById(playerId).orElseThrow(() -> new PlayerNotFoundException(playerId));
    }
}
