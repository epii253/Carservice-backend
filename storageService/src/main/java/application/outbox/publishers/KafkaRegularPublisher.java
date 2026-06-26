package application.outbox.publishers;

import application.outbox.IKafkaPublisher;
import application.outbox.ToPublishEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
public class KafkaRegularPublisher implements IKafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.write_topic}")
    private String topic;

    @Override
    public void publish(ToPublishEvent event) {
        try {
            kafkaTemplate.send(topic, event.getEventType(), event.getPayload()).get(3, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException("Kafka publish failed", ex);
        }
    }
}
