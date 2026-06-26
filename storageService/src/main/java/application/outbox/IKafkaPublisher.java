package application.outbox;

public interface IKafkaPublisher {
    void publish(ToPublishEvent event);
}