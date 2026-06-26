package domain.entities.parts;

import domain.entities.CarPart;
import domain.valueObjects.EngineType;
import domain.valueObjects.ModelName;
import domain.valueObjects.PartType;
import domain.valueObjects.Price;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity(name = "engines")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Engine extends CarPart {
    private float power;
    private float volume;

    @Enumerated(EnumType.STRING)
    @Column(name = "engine_type")
    private EngineType engineType;

    public Engine(PartType partType,
                  String name,
                  Price diffPrice,
                  List<ModelName> compatibleModels,
                  float power,
                  float volume,
                  EngineType engineType
    ) {
        super(partType, name, diffPrice, compatibleModels);

        this.power = power;
        this.volume = volume;
        this.engineType = engineType;
    }

    public Engine(Engine other) {
        super(other.getPartType(), other.getName(), other.getDiffPrice(), other.getCompatibleModels());

        power = other.power;
        volume = other.volume;
        engineType = other.engineType;
    }

    @Override
    public CarPart Clone() {
        return new Engine(this);
    }
}
