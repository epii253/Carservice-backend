package presentation.entryobjects.orders;

import lombok.NonNull;

import java.util.UUID;
public record MoveForwardOrderDto(
        @NonNull
        UUID orderId
) {
}