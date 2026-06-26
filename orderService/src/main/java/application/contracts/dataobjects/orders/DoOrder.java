package application.contracts.dataobjects.orders;

import java.util.UUID;

public class DoOrder {
    private DoOrder() {}
    public record Request(
            String modelName,
            String color
    ) {};

    public record Response(UUID orderId, UUID managerId) {};
}
