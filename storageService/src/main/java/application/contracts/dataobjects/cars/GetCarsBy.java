package application.contracts.dataobjects.cars;

import domain.entities.GearBoxType;
import domain.valueObjects.*;

import java.util.List;

public class GetCarsBy {
    private GetCarsBy() {}
    public record Request(
            Price priceLowerBound,
            Price priceUpperBound,
            BrandName brandName,
            ModelName modelName,
            CarCase carCase,
            EngineType engineType,
            Float enginePowerLowerBound,
            Float enginePowerUpperBound,
            Float engineVolumeLowerBound,
            Float engineVolumeUpperBound,
            GearBoxType gearBoxType,
            Wheeldrive wheelDrive,
            Color color
    ) {};

    public record Response(List<CarInfo> models) {};
}