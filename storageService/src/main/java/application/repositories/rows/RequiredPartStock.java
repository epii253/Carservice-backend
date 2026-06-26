package application.repositories.rows;

import domain.entities.BaseEntity;
import domain.entities.CarModel;
import domain.valueObjects.PartType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "required_part_stocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RequiredPartStock extends BaseEntity {
    @ManyToOne
    @JoinColumn(
            name = "car_model_id",
            referencedColumnName = "id"
    )
    private CarModel carModel;

    @Enumerated(EnumType.STRING)
    @Column(name = "part_type")
    private PartType partType;

    public RequiredPartStock(CarModel carModel, PartType partType) {
        this.carModel = carModel;
        this.partType = partType;
    }
}
