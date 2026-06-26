package domain.entities.parts;

import domain.entities.CarPart;
import domain.valueObjects.ModelName;
import domain.valueObjects.PartType;
import domain.valueObjects.Price;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity(name = "rudders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rudder extends CarPart {
    public Rudder(PartType partType, String name, Price diffPrice, List<ModelName> compatibleModels) {
        super(partType, name, diffPrice, compatibleModels);
    }

    public Rudder(Rudder other) {
        super(other.getPartType(), other.getName(), other.getDiffPrice(), other.getCompatibleModels());
    }

    @Override
    public CarPart Clone() {
        return new Rudder(this);
    }
}
