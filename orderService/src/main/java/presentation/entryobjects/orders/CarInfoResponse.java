package presentation.entryobjects.orders;

public record CarInfoResponse(
        String id,
        String modelName,
        String brandName,
        String color,
        float price,
        String gearBoxType,
        String carCase,
        PartInfoResponse rudderInfo,
        PartInfoResponse wheelInfo,
        PartInfoResponse transmissionInfo,
        PartInfoResponse interiorInfo,
        String wheelDrive,
        PartInfoResponse engineInfo,
        float enginePower,
        float engineVolume,
        String engineType
) {}
