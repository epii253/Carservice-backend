package application.outbox.payloads;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class EventPayload {
    public abstract String toPayload(ObjectMapper objectMapper);
}
