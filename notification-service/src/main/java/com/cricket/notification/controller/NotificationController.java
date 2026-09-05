package com.cricket.notification.controller;

import com.cricket.notification.dto.MilestoneCheckRequest;
import com.cricket.notification.dto.NotificationResponse;
import com.cricket.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Called once per ball (by a scorer/admin directly for manual testing;
     * automatically via Kafka in normal operation — see BallEventConsumer,
     * which bypasses this endpoint and its auth check entirely). Runs every
     * registered MilestoneRule and persists whichever ones fire.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SCORER')")
    @PostMapping("/check-milestone")
    public List<NotificationResponse> checkMilestone(@Valid @RequestBody MilestoneCheckRequest request) {
        return notificationService.checkMilestones(request);
    }

    @GetMapping("/match/{matchId}")
    public List<NotificationResponse> getByMatch(@PathVariable UUID matchId) {
        return notificationService.getByMatch(matchId);
    }

    @GetMapping("/player/{playerId}")
    public List<NotificationResponse> getByPlayer(@PathVariable UUID playerId) {
        return notificationService.getByPlayer(playerId);
    }

    /** Any authenticated user can mark a notification they can see as read. */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markRead(@PathVariable UUID notificationId) {
        return notificationService.markRead(notificationId);
    }
}