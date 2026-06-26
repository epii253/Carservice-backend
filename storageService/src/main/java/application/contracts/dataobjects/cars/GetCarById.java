package application.contracts.dataobjects.cars;

import lombok.Builder;
import lombok.NonNull;

import java.util.UUID;

public class GetCarById {
    private GetCarById() {}

    @Builder
    public record Request(
            UUID carId
    ) {};

    public record Response(
            @NonNull
            CarInfo carInfo
    ) {};
}