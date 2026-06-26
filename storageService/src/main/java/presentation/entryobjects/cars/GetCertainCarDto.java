package presentation.entryobjects.cars;

import lombok.NonNull;

public record GetCertainCarDto(
        @NonNull
        String modelName
) { }
