package com.cricket.notification.service.rule;

import com.cricket.notification.dto.MilestoneCheckRequest;
import com.cricket.notification.entity.Notification;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MilestoneRuleTest {

    private final FiftyRule fiftyRule = new FiftyRule();
    private final CenturyRule centuryRule = new CenturyRule();
    private final HatTrickRule hatTrickRule = new HatTrickRule();

    @Test
    void fiftyRule_firesExactlyOnBallThatCrossesFifty() {
        MilestoneCheckRequest request = battingRequest(48, 52);

        Optional<Notification> result = fiftyRule.evaluate(request);

        assertThat(result).isPresent();
    }

    @Test
    void fiftyRule_doesNotFireWhenAlreadyPastFifty() {
        MilestoneCheckRequest request = battingRequest(55, 60);

        assertThat(fiftyRule.evaluate(request)).isEmpty();
    }

    @Test
    void fiftyRule_doesNotFireWhenBallAlsoCrossesCentury() {
        // e.g. 49 -> 105 in one huge over-boundary miscount scenario:
        // century rule should own this, not fifty rule, to avoid double-firing.
        MilestoneCheckRequest request = battingRequest(49, 105);

        assertThat(fiftyRule.evaluate(request)).isEmpty();
    }

    @Test
    void centuryRule_firesExactlyOnBallThatCrossesHundred() {
        MilestoneCheckRequest request = battingRequest(97, 101);

        assertThat(centuryRule.evaluate(request)).isPresent();
    }

    @Test
    void centuryRule_doesNotFireBelowHundred() {
        MilestoneCheckRequest request = battingRequest(80, 90);

        assertThat(centuryRule.evaluate(request)).isEmpty();
    }

    @Test
    void hatTrickRule_firesOnThirdConsecutiveWicket() {
        MilestoneCheckRequest request = bowlingRequest(true, 3);

        assertThat(hatTrickRule.evaluate(request)).isPresent();
    }

    @Test
    void hatTrickRule_doesNotFireOnSecondWicketOnly() {
        MilestoneCheckRequest request = bowlingRequest(true, 2);

        assertThat(hatTrickRule.evaluate(request)).isEmpty();
    }

    @Test
    void hatTrickRule_doesNotFireWhenNoWicketThisBall() {
        MilestoneCheckRequest request = bowlingRequest(false, 3);

        assertThat(hatTrickRule.evaluate(request)).isEmpty();
    }

    private MilestoneCheckRequest battingRequest(int previousRuns, int newRuns) {
        return new MilestoneCheckRequest(UUID.randomUUID(), UUID.randomUUID(), "Test Player",
                previousRuns, newRuns, false, null, null, 0);
    }

    private MilestoneCheckRequest bowlingRequest(boolean wicketThisBall, int consecutiveWickets) {
        return new MilestoneCheckRequest(UUID.randomUUID(), UUID.randomUUID(), "Batter",
                0, 0, wicketThisBall, UUID.randomUUID(), "Test Bowler", consecutiveWickets);
    }
}
