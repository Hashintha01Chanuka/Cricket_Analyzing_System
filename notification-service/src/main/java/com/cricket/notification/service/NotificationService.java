package com.cricket.notification.service;

import com.cricket.notification.dto.MilestoneCheckRequest;
import com.cricket.notification.dto.NotificationResponse;
import com.cricket.notification.entity.Notification;
import com.cricket.notification.exception.NotificationNotFoundException;
import com.cricket.notification.repository.NotificationRepository;
import com.cricket.notification.service.rule.MilestoneRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    // Spring injects every MilestoneRule bean into this list automatically.
    // Adding a new rule class elsewhere in the package means it shows up
    // here with zero changes to this class.
    private final List<MilestoneRule> milestoneRules;
    private final NotificationRepository notificationRepository;

    @Transactional
    public List<NotificationResponse> checkMilestones(MilestoneCheckRequest request) {
        return milestoneRules.stream()
                .flatMap(rule -> rule.evaluate(request).stream())
                .map(notificationRepository::save)
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getByMatch(UUID matchId) {
        return notificationRepository.findByMatchIdOrderByCreatedAtDesc(matchId)
                .stream().map(NotificationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getByPlayer(UUID playerId) {
        return notificationRepository.findByPlayerIdOrderByCreatedAtDesc(playerId)
                .stream().map(NotificationResponse::from).toList();
    }

    @Transactional
    public NotificationResponse markRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        notification.setRead(true);
        return NotificationResponse.from(notificationRepository.save(notification));
    }
}
