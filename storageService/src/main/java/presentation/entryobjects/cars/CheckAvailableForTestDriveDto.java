package presentation.entryobjects.cars;

import lombok.NonNull;

import java.util.UUID;

public record CheckAvailableForTestDriveDto(
        @NonNull
        UUID modelId
) { }
