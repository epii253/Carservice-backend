package domain.entities;

import domain.utilities.ModelNameListConverter;
import domain.valueObjects.ModelName;
import domain.valueObjects.PartType;
import domain.valueObjects.Price;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class CarPart extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "part_type")
    private PartType partType;

    @Column(name = "part_name")
    private String name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "price"))
    private Price diffPrice;

    @Convert(converter = ModelNameListConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "compatible_models", columnDefinition = "jsonb", nullable = false)
    private List<ModelName> compatibleModels;

    public boolean IsCompatible(ModelName modelName) {
        return compatibleModels.stream().map(ModelName::getName).anyMatch(x -> Objects.equals(modelName.getName(), x));
    }

    public CarPart(PartType partType, String name, Price diffPrice, List<ModelName> compatibleModels) {
        this.partType = partType;
        this.name = name;
        this.diffPrice = new Price(diffPrice);
        this.compatibleModels = compatibleModels.stream().map(ModelName::new).toList();
    }

    public CarPart(CarPart other) {
        partType = other.partType;
        name = String.valueOf(other.name);
        compatibleModels = new ArrayList<>(other.compatibleModels);
        diffPrice = new Price(other.diffPrice);
    }

    public abstract CarPart Clone();
}
