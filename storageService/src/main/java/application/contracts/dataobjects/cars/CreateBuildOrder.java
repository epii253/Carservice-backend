package application.contracts.dataobjects.cars;

import domain.entities.Pair;

import java.util.List;
import java.util.UUID;

public class CreateBuildOrder {
    private CreateBuildOrder() {}
    public record Request(
            UUID orderId,
            String orderType,
            UUID modelId,
            List<Pair<String, UUID>> parts
    ) {};

    public record Response(boolean isSuccess) {};
}
