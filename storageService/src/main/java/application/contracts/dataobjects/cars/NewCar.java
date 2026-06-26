package application.contracts.dataobjects.cars;

import domain.entities.GearBoxType;
import domain.valueObjects.*;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public class NewCar {
    private NewCar() {}
    public record Request(
            @NonNull List<PartType> requiredParts,
            @NonNull ModelName modelName,
            @NonNull CarCase carCase,
            @NonNull BrandName brandName,

            @NonNull Color color,
            @NonNull Price initialPrice,

            @NonNull Wheeldrive wheeldrive,
            @NonNull GearBoxType gearBoxType,
            @NonNull UUID rudderId,
            @NonNull UUID wheelId,
            @NonNull UUID transmissionId,
            @NonNull UUID interiorId,
            @NonNull UUID engineId
    ) {};

    public record Response(CarInfo carInfo) {};
}
