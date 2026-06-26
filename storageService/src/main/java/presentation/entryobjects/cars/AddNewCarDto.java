package presentation.entryobjects.cars;

import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public record AddNewCarDto(
        @NonNull List<String> requiredParts,
        @NonNull String modelName,
        @NonNull String carCase,
        @NonNull String brandName,

        @NonNull String color,
        @NonNull Float initialPrice,

        @NonNull String wheeldrive,
        @NonNull String gearBoxType,
        @NonNull UUID rudderId,
        @NonNull UUID wheelId,
        @NonNull UUID transmissionId,
        @NonNull UUID interiorId,
        @NonNull UUID engineId
) { }
