package application.outbox.consumers;

import application.contracts.dataobjects.cars.CreateBuildOrder;
import application.outbox.payloads.CarOrderEventPayload;
import application.services.CarService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private final CarService carService;

    @KafkaListener(topics = "${app.kafka.listen_topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void handle(
            String payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String eventType
    ) throws Exception {
        if (payload == null || payload.trim().isEmpty()) {
            log.error("Critical Error of empty message from eventType: {}", eventType);
            return;
        }

        JsonNode json = objectMapper.readTree(payload);

        var event = objectMapper.readValue(payload, CarOrderEventPayload.class);

        switch (eventType) {
            case "OrderSentForApproval":
                carService.CreateBuildOrder(
                        new CreateBuildOrder.Request(
                                event.getOrderId(),
                                event.getOrderType(),
                                event.getModelId(),
                                event.getParts()
                        )
                );
            break;

            default:
                log.warn(String.format("Unknown event %s", eventType));
            break;
        }
    }
}