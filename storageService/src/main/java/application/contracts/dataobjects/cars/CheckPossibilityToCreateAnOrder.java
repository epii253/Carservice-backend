package application.contracts.dataobjects.cars;

import domain.entities.Pair;
import domain.valueObjects.Color;
import domain.valueObjects.ModelName;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public class CheckPossibilityToCreateAnOrder {
    private CheckPossibilityToCreateAnOrder() {}

    @Builder
    public record Request(
            @NonNull
            ModelName modelName,
            @NonNull Color color,
            UUID rudderId,
            UUID wheelId,
            UUID transmissionId,
            UUID interiorId,
            UUID engineId
    ) {};

    public record Response(
            UUID modelId,
            List<Pair<String, UUID>> parts
    ) {};
}
