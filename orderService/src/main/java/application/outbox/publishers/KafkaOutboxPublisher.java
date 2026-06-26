package application.outbox.publishers;


import application.outbox.OutboxPublisher;
import application.repositories.rows.OutboxEvent;
import lombok.RequiredArgsConstructor;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaOutboxPublisher implements OutboxPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.write_topic}")
    private String topic;

    @Override
    public void publish(OutboxEvent event) {
        try {
            kafkaTemplate.send(topic, event.getEventType(), event.getPayload()).get(3, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException("Kafka publish failed", ex);
        }
    }
}