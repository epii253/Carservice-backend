package application.contracts.dataobjects.cars;

import lombok.Builder;
import lombok.NonNull;

import java.util.UUID;

public class CheckAvailableForTestDriven {
    private CheckAvailableForTestDriven() {}

    @Builder
    public record Request(
            @NonNull
            UUID modelId
    ) {};

    public record Response(
            @NonNull
            UUID modelId,

            @NonNull
            Boolean isAvailable
    ) {};
}
