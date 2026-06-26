package application.contracts.dataobjects.cars;

import domain.valueObjects.ModelName;

import java.util.List;

public class GetCertainCar {
    private GetCertainCar() {}
    public record Request(ModelName modelName) {};

    public record Response(List<CarInfo> models) {};
}