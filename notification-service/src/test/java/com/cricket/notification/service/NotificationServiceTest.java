package com.cricket.notification.service;

import com.cricket.notification.dto.MilestoneCheckRequest;
import com.cricket.notification.dto.NotificationResponse;
import com.cricket.notification.entity.Notification;
import com.cricket.notification.entity.NotificationType;
import com.cricket.notification.repository.NotificationRepository;
import com.cricket.notification.service.rule.MilestoneRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private MilestoneRule firingRule;

    @Mock
    private MilestoneRule silentRule;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(List.of(firingRule, silentRule), notificationRepository);
    }

    @Test
    void checkMilestones_onlyPersistsRulesThatFire() {
        UUID matchId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        MilestoneCheckRequest request = new MilestoneCheckRequest(matchId, playerId, "Player",
                48, 52, false, null, null, 0);

        Notification fifty = Notification.builder()
                .type(NotificationType.FIFTY).matchId(matchId).playerId(playerId)
                .message("Fifty!").build();

        when(firingRule.evaluate(request)).thenReturn(Optional.of(fifty));
        when(silentRule.evaluate(request)).thenReturn(Optional.empty());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        List<NotificationResponse> result = notificationService.checkMilestones(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(NotificationType.FIFTY);
    }

    @Test
    void checkMilestones_returnsEmptyListWhenNoRuleFires() {
        MilestoneCheckRequest request = new MilestoneCheckRequest(UUID.randomUUID(), UUID.randomUUID(),
                "Player", 10, 12, false, null, null, 0);

        when(firingRule.evaluate(request)).thenReturn(Optional.empty());
        when(silentRule.evaluate(request)).thenReturn(Optional.empty());

        List<NotificationResponse> result = notificationService.checkMilestones(request);

        assertThat(result).isEmpty();
    }
}
