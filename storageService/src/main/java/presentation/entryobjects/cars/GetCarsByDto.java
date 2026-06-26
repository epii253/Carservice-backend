package presentation.entryobjects.cars;

public record GetCarsByDto(
        Float priceLowerBound,
        Float priceUpperBound,
        String brandName,
        String modelName,
        String carCase,
        String engineType,
        Float enginePowerLowerBound,
        Float enginePowerUpperBound,
        Float engineVolumeLowerBound,
        Float engineVolumeUpperBound,
        String gearBoxType,
        String wheelDrive,
        String color
) {}
