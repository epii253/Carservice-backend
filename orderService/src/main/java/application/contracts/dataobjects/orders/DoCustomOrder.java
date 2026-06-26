package application.contracts.dataobjects.orders;

import domain.valueObjects.Color;
import domain.valueObjects.ModelName;
import lombok.NonNull;

import java.util.UUID;

public class DoCustomOrder {
    private DoCustomOrder() {}
    public record Request(@NonNull ModelName modelName,
                          @NonNull Color color,
                          UUID rudderId,
                          UUID wheelId,
                          UUID transmissionId,
                          UUID interiorId,
                          UUID engineId) {}

    public record Response(UUID orderId, UUID managerId) {}
}