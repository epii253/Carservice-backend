package presentation.entryobjects.cars;

import domain.valueObjects.Color;
import lombok.NonNull;

import java.util.UUID;

public record CheckPossibilityToCreateAnOrderDto(
    @NonNull
    String modelName,
    @NonNull Color color,
    UUID rudderId,
    UUID wheelId,
    UUID transmissionId,
    UUID interiorId,
    UUID engineId
) { }
