package application.contracts.dataobjects.cars;

public record CarInfo(
        String id,
        String modelName,
        String brandName,
        String color,
        float price,
        String gearBoxType,
        String carCase,
        PartInfo rudderInfo,
        PartInfo wheelInfo,
        PartInfo transmissionInfo,
        PartInfo interiorInfo,
        String wheelDrive,
        PartInfo engineInfo,
        float enginePower,
        float engineVolume,
        String engineType
) {}
