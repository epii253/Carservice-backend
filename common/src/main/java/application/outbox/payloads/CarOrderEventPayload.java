package application.outbox.payloads;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.entities.Pair;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class CarOrderEventPayload extends EventPayload {
    UUID orderId;
    String orderType;
    UUID modelId;
    List<Pair<String, UUID>> parts;

    @Override
    public String toPayload(ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize payload", e);
        }
    }
}
