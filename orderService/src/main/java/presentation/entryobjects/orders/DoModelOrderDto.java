package presentation.entryobjects.orders;

import lombok.NonNull;

import java.util.UUID;

public record DoModelOrderDto(
        @NonNull
        String modelName,

        @NonNull
        String color
) { }
