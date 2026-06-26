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

@Entity(name = "interiors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interior extends CarPart {
    public Interior(PartType partType, String name, Price diffPrice, List<ModelName> compatibleModels) {
        super(partType, name, diffPrice, compatibleModels);
    }

    public Interior(Interior other) {
        super(other.getPartType(), other.getName(), other.getDiffPrice(), other.getCompatibleModels());
    }

    @Override
    public CarPart Clone() {
        return new Interior(this);
    }
}
