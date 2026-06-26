package application.contracts.dataobjects.cars;

import lombok.Builder;

import java.util.List;

public class GetAllCars {
    private GetAllCars() {}

    @Builder
    public record Request() {};

    public record Response(List<CarInfo> models) {};
}
