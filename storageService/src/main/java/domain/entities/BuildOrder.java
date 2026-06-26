package domain.entities;

import domain.utilities.CarJsonConverter;
import domain.valueObjects.PartType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Getter
@Entity(name = "build_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BuildOrder extends BaseEntity {
    @Column(
            name = "source_order_id",
            nullable = false,
            unique = true
    )
    private UUID sourceOrderId;

    @Column(
            name = "order_type",
            nullable = false
    )
    private String orderType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = CarJsonConverter.class)
    @Column(name = "car", columnDefinition = "jsonb")
    private Car car;

    @ElementCollection(targetClass = PartType.class)
    @CollectionTable(
            name = "build_order_required_parts",
            joinColumns = @JoinColumn(name = "build_order_id")
    )
    @Column(name = "required_parts")
    @Enumerated(EnumType.STRING)
    private List<PartType> requiredParts;

    @Column(
            name = "in_charge",
            nullable = false
    )
    private UUID inCharge;

    @Enumerated(EnumType.STRING)
    @Column(name = "build_order_status")
    private BuildOrderStatus buildOrderStatus;

    public BuildOrder(
            UUID sourceOrderId,
            String orderType,
            Car car,
            List<PartType> requiredParts,
            UUID inCharge,
            BuildOrderStatus buildOrderStatus) {
        this.sourceOrderId = sourceOrderId;
        this.orderType = orderType;
        this.car = car;
        this.requiredParts = requiredParts;
        this.inCharge = inCharge;
        this.buildOrderStatus = buildOrderStatus;
    }
}
