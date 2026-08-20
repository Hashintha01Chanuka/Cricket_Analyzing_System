package com.cricket.player.service;

import com.cricket.player.dto.*;
import com.cricket.player.entity.CareerStats;
import com.cricket.player.entity.Player;
import com.cricket.player.entity.PlayerRole;
import com.cricket.player.exception.PlayerNotFoundException;
import com.cricket.player.repository.CareerStatsRepository;
import com.cricket.player.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private CareerStatsRepository careerStatsRepository;

    @InjectMocks
    private PlayerService playerService;

    private UUID playerId;
    private Player player;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        player = Player.builder()
                .id(playerId)
                .name("Virat Kohli")
                .country("India")
                .role(PlayerRole.BATSMAN)
                .build();
    }

    @Test
    void createPlayer_savesPlayerAndZeroedStats() {
        when(playerRepository.save(any(Player.class))).thenReturn(player);
        when(careerStatsRepository.save(any(CareerStats.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PlayerRequest request = new PlayerRequest("Virat Kohli", "India",
                PlayerRole.BATSMAN, "Right-hand bat", null, null);

        PlayerResponse response = playerService.createPlayer(request);

        assertThat(response.name()).isEqualTo("Virat Kohli");
        ArgumentCaptor<CareerStats> captor = ArgumentCaptor.forClass(CareerStats.class);
        verify(careerStatsRepository).save(captor.capture());
        assertThat(captor.getValue().getRunsScored()).isZero();
    }

    @Test
    void getPlayer_throwsWhenNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(playerRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.getPlayer(unknownId))
                .isInstanceOf(PlayerNotFoundException.class);
    }

    @Test
    void updateCareerStats_accumulatesBattingDeltaAndDetectsCentury() {
        CareerStats existing = CareerStats.builder().player(player).runsScored(40).innings(1).build();
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(careerStatsRepository.findByPlayerId(playerId)).thenReturn(Optional.of(existing));
        when(careerStatsRepository.save(any(CareerStats.class))).thenAnswer(inv -> inv.getArgument(0));

        CareerStatsUpdateRequest request = new CareerStatsUpdateRequest(105, 90, true, 0, 0, 0);

        CareerStatsResponse response = playerService.updateCareerStats(playerId, request);

        assertThat(response.runsScored()).isEqualTo(145); // 40 + 105
        assertThat(response.innings()).isEqualTo(2);
        assertThat(response.centuries()).isEqualTo(1);
    }

    @Test
    void topByRuns_returnsHighestScorersInDescendingOrder() {
        Player p1 = Player.builder().id(UUID.randomUUID()).name("A").build();
        Player p2 = Player.builder().id(UUID.randomUUID()).name("B").build();
        Player p3 = Player.builder().id(UUID.randomUUID()).name("C").build();

        List<CareerStats> all = List.of(
                CareerStats.builder().player(p1).runsScored(200).build(),
                CareerStats.builder().player(p2).runsScored(500).build(),
                CareerStats.builder().player(p3).runsScored(300).build()
        );
        when(careerStatsRepository.findAllWithPlayer()).thenReturn(all);

        List<LeaderboardEntry> top2 = playerService.topByRuns(2);

        assertThat(top2).hasSize(2);
        assertThat(top2.get(0).name()).isEqualTo("B");
        assertThat(top2.get(0).value()).isEqualTo(500);
        assertThat(top2.get(1).name()).isEqualTo("C");
        assertThat(top2.get(1).value()).isEqualTo(300);
    }
}
