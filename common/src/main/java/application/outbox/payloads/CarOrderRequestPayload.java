package application.outbox.payloads;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class CarOrderRequestPayload extends EventPayload {
    UUID orderId;

    @Override
    public String toPayload(ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize payload", e);
        }
    }
}
