package application.contracts.dataobjects.orders;

import java.util.List;
import java.util.UUID;

public class MoveFrowardOrder {
    private MoveFrowardOrder() {}
    public record Request(UUID orderId) {};

    public record Response(String status) {};
}
