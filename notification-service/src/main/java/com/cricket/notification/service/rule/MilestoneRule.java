package com.cricket.notification.service.rule;

import com.cricket.notification.dto.MilestoneCheckRequest;
import com.cricket.notification.entity.Notification;

import java.util.Optional;

/**
 * Strategy pattern: each milestone type (fifty, century, hat-trick) is its
 * own rule implementing this interface, and NotificationService just runs
 * every registered rule against each request. Adding a new milestone type
 * later (e.g. "five-wicket haul") means writing one new class and nothing
 * else changes — NotificationService, the controller, and the existing
 * rules are all untouched. That's the Open/Closed Principle in practice,
 * and a concrete example worth having ready for an OOP interview question.
 */
public interface MilestoneRule {

    Optional<Notification> evaluate(MilestoneCheckRequest request);
}
