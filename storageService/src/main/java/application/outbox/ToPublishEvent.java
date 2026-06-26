package application.outbox;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ToPublishEvent {
    private String eventType;

    private String payload;
}
