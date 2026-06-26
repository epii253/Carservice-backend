package domain.entities.parts;

import domain.entities.CarPart;
import domain.entities.GearBoxType;
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

@Entity(name = "gear_boxes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GearBox extends CarPart {

    @Enumerated(EnumType.STRING)
    @Column(name = "gear_box_type")
    private GearBoxType gearBoxType;

    public GearBox(PartType partType, String name, Price diffPrice, List<ModelName> compatibleModels, GearBoxType gearBoxType) {
        super(partType, name, diffPrice, compatibleModels);
        this.gearBoxType = gearBoxType;
    }

    public GearBox(GearBox other) {
        super(other.getPartType(), other.getName(), other.getDiffPrice(), other.getCompatibleModels());

        this.gearBoxType = other.gearBoxType;
    }

    @Override
    public CarPart Clone() {
        return new GearBox(this);
    }
}
