package domain.entities.parts;

import domain.entities.CarPart;
import domain.valueObjects.ModelName;
import domain.valueObjects.PartType;
import domain.valueObjects.Price;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity(name = "wheels")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wheel extends CarPart {
    public Wheel(PartType partType, String name, Price diffPrice, List<ModelName> compatibleModels) {
        super(partType, name, diffPrice, compatibleModels);
    }

    public Wheel(Wheel other) {
        super(other.getPartType(), other.getName(), other.getDiffPrice(), other.getCompatibleModels());
    }

    @Override
    public CarPart Clone() {
        return new Wheel(this);
    }
}
