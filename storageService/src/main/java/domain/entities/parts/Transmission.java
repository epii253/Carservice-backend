package domain.entities.parts;

import domain.entities.CarPart;
import domain.valueObjects.ModelName;
import domain.valueObjects.PartType;
import domain.valueObjects.Price;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity(name = "transmissions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transmission extends CarPart {
    public Transmission(PartType partType, String name, Price diffPrice, List<ModelName> compatibleModels) {
        super(partType, name, diffPrice, compatibleModels);
    }

    public Transmission(Transmission other) {
        super(other.getPartType(), other.getName(), other.getDiffPrice(), other.getCompatibleModels());
    }

    @Override
    public CarPart Clone() {
        return new Transmission(this);
    }
}
