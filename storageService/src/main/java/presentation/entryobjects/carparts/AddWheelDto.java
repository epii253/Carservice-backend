package presentation.entryobjects.carparts;

import lombok.NonNull;

import java.util.List;

public record AddWheelDto(
        @NonNull String name,
        @NonNull Float diffPrice,
        @NonNull List<String> compatibleModels
) {
}