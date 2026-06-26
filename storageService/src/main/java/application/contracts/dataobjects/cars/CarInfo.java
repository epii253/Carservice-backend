package application.contracts.dataobjects.cars;

import application.contracts.dataobjects.carparts.PartInfo;
import lombok.NonNull;

import java.util.UUID;

public record CarInfo(
    @NonNull
    UUID id,
    @NonNull
    String modelName,

    @NonNull
    String brandName,

    @NonNull
    String color,

    @NonNull
    Float price,

    @NonNull
    String gearBoxType,

    @NonNull
    String carCase,

    @NonNull
    PartInfo rudderInfo,

    @NonNull
    PartInfo wheelInfo,

    @NonNull
    PartInfo transmissionInfo,

    @NonNull
    PartInfo interiorInfo,

    @NonNull
    String wheelDrive,

    @NonNull
    PartInfo engineInfo,

    @NonNull
    Float enginePower,

    @NonNull
    Float engineVolume,

    @NonNull
    String engineType
) { }
