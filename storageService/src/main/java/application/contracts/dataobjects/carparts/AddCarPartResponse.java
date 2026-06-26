package application.contracts.dataobjects.carparts;

import lombok.NonNull;

import java.util.UUID;

public record AddCarPartResponse(
        @NonNull
        UUID partId
) {}