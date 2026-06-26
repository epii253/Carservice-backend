package presentation.entryobjects.carparts;

import lombok.NonNull;

import java.util.List;

public record AddEngineDto(
        @NonNull String name,
        @NonNull Float diffPrice,
        @NonNull List<String> compatibleModels,
        @NonNull Float power,
        @NonNull Float volume,
        @NonNull String engineType
) { }
