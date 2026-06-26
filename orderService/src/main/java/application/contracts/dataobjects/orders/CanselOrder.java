package application.contracts.dataobjects.orders;

import lombok.NonNull;

import java.util.UUID;

public class CanselOrder {
    private CanselOrder() {}
    public record Request(
            @NonNull
            UUID orderId
    ) {}

    public record Response() {}
}
