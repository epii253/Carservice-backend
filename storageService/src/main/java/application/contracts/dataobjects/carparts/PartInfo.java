package application.contracts.dataobjects.carparts;

import lombok.NonNull;

import java.util.UUID;

public record PartInfo (
        @NonNull
        String name,
        @NonNull
        String partType,
        @NonNull
        UUID id
) { }
