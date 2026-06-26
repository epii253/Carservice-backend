package domain.entities;

import domain.valueObjects.ModelName;
import domain.valueObjects.PartType;
import domain.valueObjects.Price;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class Car {
    private ModelName modelName;
    private List<Pair<PartType, UUID>> parts;

    private Price price;

    @com.fasterxml.jackson.annotation.JsonCreator
    public Car(
            @com.fasterxml.jackson.annotation.JsonProperty("modelName") ModelName modelName,
            @com.fasterxml.jackson.annotation.JsonProperty("parts") List<Pair<PartType, UUID>> parts,
            @com.fasterxml.jackson.annotation.JsonProperty("price") Price price
    ) {
        this.modelName = new ModelName(modelName);
        this.parts = (parts != null) ? List.copyOf(parts) : List.of();
        this.price = new Price(price);
    }

    public Car(Car other){
        this.modelName = new ModelName(other.modelName);
        this.parts = List.copyOf(other.parts);

        this.price = new Price(other.price);
    }
}
