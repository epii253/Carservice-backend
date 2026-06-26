package presentation.entryobjects.testsdrive;

import lombok.NonNull;

import java.util.UUID;

public record TestDriveRequestDto(
        @NonNull
        UUID userId,

        @NonNull
        UUID modelId
) { }
