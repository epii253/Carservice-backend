package application.contracts.dataobjects.carparts;

import domain.valueObjects.EngineType;
import domain.valueObjects.ModelName;
import domain.valueObjects.Price;
import lombok.NonNull;

import java.util.List;

public record AddEngineRequest (
        @NonNull
        String name,
        @NonNull
        Price diffPrice,
        @NonNull
        List<ModelName> compatibleModels,

        float power,
        float volume,

        EngineType engineType
) {}