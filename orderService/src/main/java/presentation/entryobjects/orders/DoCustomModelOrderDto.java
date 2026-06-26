package presentation.entryobjects.orders;

import lombok.Builder;
import lombok.NonNull;

import java.util.UUID;

public record DoCustomModelOrderDto(
        @NonNull String modelName,
        @NonNull String color,

        UUID rudderId,
        UUID wheelId,
        UUID transmissionId,
        UUID interiorId,
        UUID engineId
) {
    @Builder(setterPrefix = "with", builderMethodName = "builder")
    public static DoCustomModelOrderDto create(
            String modelName, String color,
            UUID rudderId, UUID wheelId, UUID transmissionId,
            UUID interiorId, UUID engineId) {
        return new DoCustomModelOrderDto(modelName, color, rudderId, wheelId, transmissionId, interiorId, engineId);
    }
}