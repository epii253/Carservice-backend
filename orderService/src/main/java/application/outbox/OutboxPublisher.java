package application.outbox;

import application.repositories.rows.OutboxEvent;

public interface OutboxPublisher {
    void publish(OutboxEvent event);
}