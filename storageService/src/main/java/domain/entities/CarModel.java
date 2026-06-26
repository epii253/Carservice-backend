package domain.entities;

import domain.entities.parts.*;
import domain.valueObjects.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Getter
@Entity(name = "car_models")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CarModel extends BaseEntity {

    @ElementCollection(targetClass = PartType.class)
    @CollectionTable(
            name = "required_part_model_stock",
            joinColumns = @JoinColumn(name = "car_model_id")
    )
    @Column(name = "required_parts")
    @Enumerated(EnumType.STRING)
    private List<PartType> requiredParts;

    @Embedded
    @AttributeOverride(name = "name", column = @Column(name = "model_name"))
    private ModelName modelName;

    @Embedded
    @AttributeOverride(name = "name", column = @Column(name = "car_case"))
    private CarCase carCase;

    @ManyToOne
    @JoinColumn(
            name = "rudder_id",
            referencedColumnName = "id"
    )
    private Rudder rudder;

    @ManyToOne
    @JoinColumn(
            name = "wheel_id",
            referencedColumnName = "id"
    )
    private Wheel wheels;

    @ManyToOne
    @JoinColumn(
            name = "transmission_id",
            referencedColumnName = "id"
    )
    private Transmission transmission;

    @ManyToOne
    @JoinColumn(
            name = "interior_id",
            referencedColumnName = "id"
    )
    private Interior interior;


    @Enumerated(EnumType.STRING)
    @Column(name = "wheel_drive")
    private Wheeldrive wheeldrive;

    @ManyToOne
    @JoinColumn(
            name = "engine_id",
            referencedColumnName = "id"
    )
    private Engine engine;

    @Embedded
    @AttributeOverride(name = "name", column = @Column(name = "brand_name"))
    private BrandName brandName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gear_box_type")
    private GearBoxType gearBoxType;

    @Embedded
    @AttributeOverride(name = "name", column = @Column(name = "color"))
    private Color color;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "price"))
    private Price price;

    @Builder
    public CarModel(@NonNull @Singular List<PartType> requiredParts,
                    @NonNull ModelName modelName,
                    @NonNull CarCase carCase,
                    @NonNull BrandName brandName,
                    @NonNull Engine engine,
                    @NonNull Rudder rudder,
                    @NonNull Interior interior,
                    @NonNull Transmission transmission,
                    @NonNull Wheel wheel,
                    @NonNull Wheeldrive wheeldrive,
                    @NonNull Color color,
                    @NonNull Price initialPrice,
                    @NonNull GearBoxType gearBoxType) {

        this.price = new Price(initialPrice);

        this.requiredParts = new ArrayList<>(requiredParts);
        this.modelName = modelName;
        this.carCase = carCase;
        this.brandName = new BrandName(brandName);
        this.wheeldrive = wheeldrive;
        this.color = new Color(color);

        this.gearBoxType = gearBoxType;
        this.engine = engine;
        this.rudder = rudder;
        this.wheels = wheel;
        this.transmission = transmission;
        this.interior = interior;
    }

    public CarModel(@NonNull CarModel other) {
        requiredParts = new ArrayList<>(other.requiredParts);
        modelName = new ModelName(other.modelName);
        carCase = new CarCase(other.carCase);

        brandName = new BrandName(other.brandName);
        color = new Color(other.color);
        wheeldrive = other.wheeldrive;

        price = new Price(other.price);

        this.engine = new Engine(other.engine);
        this.gearBoxType = other.gearBoxType;
        this.rudder = new Rudder(other.rudder);
        this.wheels = new Wheel(other.wheels);
        this.transmission = new Transmission(other.transmission);
        this.interior = new Interior(other.interior);
    }

    public boolean CheckRequired(List<CarPart> configurationParts) {
        if (requiredParts.isEmpty()) {
            return true;
        }
        return new HashSet<>(configurationParts.stream().map(CarPart::getPartType).toList()).containsAll(requiredParts);
    }

    public boolean CheckCompatibility(List<CarPart> parts) {
        return parts.stream().allMatch(part -> part.IsCompatible(modelName));
    }
}
