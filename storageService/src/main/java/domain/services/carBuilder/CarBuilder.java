package domain.services.carBuilder;

import domain.entities.Car;
import domain.entities.CarModel;
import domain.entities.CarPart;
import domain.entities.Pair;
import domain.valueObjects.PartType;
import domain.valueObjects.Price;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CarBuilder {
    private CarModel carModel;

    private final Map<PartType, Pair<CarPart, Price>> parts;
    private final Map<PartType, CarPart> defaultParts;

    private CarBuilder(@NonNull CarModel carModel, @NonNull Map<PartType, CarPart> defaultParts) {
        this.carModel = carModel;
        this.parts = new HashMap<>();
        this.defaultParts = Map.copyOf(defaultParts);
    }

    public static CarBuilder Create(@NonNull CarModel model, @NonNull Map<PartType, CarPart> defaultParts) {
        return new CarBuilder(model, defaultParts);
    }

    public CarBuilder WithModel(@NonNull CarModel model) {
        this.carModel = new CarModel(model);
        return this;
    }

    public CarBuilder WithPart(@NonNull CarPart carPart, @NonNull Price priceDiff) {
        this.parts.put(carPart.getPartType(), new Pair<CarPart, Price>(carPart, priceDiff));
        return this;
    }

    public Car Build() {
        if (!carModel.CheckRequired(
                Stream.concat(parts.values().stream().map(Pair::getFirst), defaultParts.values().stream()).toList()
        )) {
            throw new DomainOptionalValidationException("not all there");
        }

        if (!carModel.CheckCompatibility(parts.values().stream().map(Pair::getFirst).toList())) {
            throw new IncompatibleComponentException("there is smt is not incompatible");
        }

        Price resultPrice = new Price(carModel.getPrice());
        Map<PartType, CarPart> resultParts = new HashMap<>();

        for (var type : defaultParts.keySet()) {
            if (!resultParts.containsKey(type)) {
                resultParts.put(type, defaultParts.get(type));
            }
        }

        for (var type : parts.keySet()) {
            resultParts.put(type, parts.get(type).getFirst());

            resultPrice.DiffPrice(parts.get(type).getSecond());
        }

        for (var type : carModel.getRequiredParts()) {
            if (!resultParts.containsKey(type)) {
                throw new DomainValidationException(String.format("%s is missed", type.toString()));
            }
        }

        return new Car(carModel.getModelName(), resultParts.values().stream().map(x -> new Pair<>(x.getPartType(), x.getId())) .toList(), resultPrice);
    }
}
