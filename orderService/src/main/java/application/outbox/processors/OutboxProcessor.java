package application.outbox.processors;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import application.outbox.OutboxPublisher;
import application.repositories.IOutboxRepo;
import application.repositories.rows.OutboxEvent;
import domain.entities.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private final IOutboxRepo outboxEventRepository;
    private final OutboxPublisher outboxPublisher;

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:5000}")
    @Transactional
    public void processBatch() {
        Instant now = Instant.now();
        List<OutboxEvent> events = outboxEventRepository.findReady(
                List.of(OutboxStatus.NEW.name(), OutboxStatus.FAILED.name()), now);

        for (OutboxEvent event : events) {
            try {
                log.info("Outbox try publish id={}", event.getId());

                outboxPublisher.publish(event);
                markSent(event, now);
            } catch (Exception ex) {
                markFailed(event, now, ex);
            }
        }
    }

    private void markSent(OutboxEvent event, Instant now) {
        event.setStatus(OutboxStatus.SENT.name());
        event.setUpdatedAt(now);

        outboxEventRepository.save(event);
    }

    private void markFailed(OutboxEvent event, Instant now, Exception ex) {
        int nextRetry = event.getRetryCount() + 1;

        var backoff = retryBackoff(nextRetry);
        event.setRetryCount(nextRetry);
        event.setStatus(OutboxStatus.FAILED.name());
        event.setAvailableAt(now.plus(backoff));
        event.setUpdatedAt(now);

        outboxEventRepository.save(event);

        log.warn("Outbox publish failed id={}, retry in {}s", event.getId(), backoff.toSeconds(), ex);
    }

    private Duration retryBackoff(int retryCount) {
        if (retryCount <= 3)
            return Duration.ofSeconds(15);
        if (retryCount <= 6)
            return Duration.ofMinutes(1);
        if (retryCount <= 10)
            return Duration.ofMinutes(5);
        if (retryCount <= 15)
            return Duration.ofMinutes(15);
        return Duration.ofHours(1);
    }

}