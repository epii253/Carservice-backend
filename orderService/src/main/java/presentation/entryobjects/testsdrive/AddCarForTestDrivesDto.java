package presentation.entryobjects.testsdrive;

import lombok.NonNull;

import java.util.UUID;

public record AddCarForTestDrivesDto(
        @NonNull
        UUID carId,
        @NonNull
        UUID userId
) { }
