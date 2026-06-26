package application.contracts.dataobjects.carparts;

import domain.valueObjects.ModelName;
import domain.valueObjects.Price;
import lombok.NonNull;

import java.util.List;

public record AddRudderRequest (
    @NonNull
    String name,
    @NonNull
    Price diffPrice,
    @NonNull
    List<ModelName> compatibleModels
) {}
