package application.outbox.consumers;

import application.contracts.dataobjects.orders.CanselOrder;
import application.contracts.dataobjects.orders.MoveFrowardOrder;
import application.contracts.ports.IOrderService;
import application.outbox.payloads.CarOrderRequestPayload;
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
    private final IOrderService orderService;

    @KafkaListener(topics = "${app.kafka.listen_topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void handle(
            String payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String eventType
    ) throws  Exception {
        if (payload == null || payload.trim().isEmpty()) {
            log.error("Critical Error of empty message from eventType: {}", eventType);
            return;
        }

        JsonNode json = objectMapper.readTree(payload);

        switch (eventType) {
            case "OrderApproved":
                var eventApproved = objectMapper.readValue(payload, CarOrderRequestPayload.class);

                orderService.MoveFrowardOrder(new MoveFrowardOrder.Request(
                            eventApproved.getOrderId()
                        )
                );
            break;

            case "OrderRejected":
                var eventRejected = objectMapper.readValue(payload, CarOrderRequestPayload.class);

                orderService.CanselOrder(new CanselOrder.Request(
                            eventRejected.getOrderId()
                        )
                );
            break;

            default:
                log.warn(String.format("Unknown event %s", eventType));
            break;
        }

    }
}