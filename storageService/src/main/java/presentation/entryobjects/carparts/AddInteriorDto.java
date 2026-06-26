package presentation.entryobjects.carparts;

import lombok.NonNull;

import java.util.List;

public record AddInteriorDto(
        @NonNull String name,
        @NonNull Float diffPrice,
        @NonNull List<String> compatibleModels
) { }
