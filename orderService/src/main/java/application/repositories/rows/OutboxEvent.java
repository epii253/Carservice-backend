package application.repositories.rows;

import domain.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.Instant;

@Getter
@Entity(name = "outbox_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent extends BaseEntity {

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    @Setter
    private String status;

    @Column(nullable = false)
    @Setter
    private int retryCount;

    @Column(nullable = false)
    @Setter
    private Instant availableAt;

    public OutboxEvent(String eventType, String payload, String status, int retryCount, Instant availableAt) {
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.availableAt = availableAt;
    }
}
