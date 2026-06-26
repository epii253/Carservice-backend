package domain.entities;

import application.repositories.rows.User;
import domain.untilities.PartsListConverter;
import domain.valueObjects.OrderType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Getter
@Entity(name = "car_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CarOrder extends BaseEntity {
    @ManyToOne
    @JoinColumn(
            name = "customer_id",
            referencedColumnName = "id"
    )
    private User user;

    @ManyToOne
    @JoinColumn(
            name = "manager_id",
            referencedColumnName = "id"
    )
    private User manger;

    /*@JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "car",
            nullable = false
    )
    private JsonNode car;
    */

    @Column(
            nullable = false
    )
    private UUID modelId;

    @Convert(converter = PartsListConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parts", columnDefinition = "jsonb", nullable = false)
    private List<Pair<String, UUID>> parts;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type")
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private OrderState state;

    public CarOrder(
            @NonNull User user,
            @NonNull User manger,
            @NonNull OrderType orderType,
            @NonNull List<Pair<String, UUID>> parts,
            @NonNull UUID modelId
    ) {
        this.user = user;
        this.manger = manger;
        this.parts = parts;
        this.modelId = modelId;
        this.orderType = orderType;

        state = OrderState.Registered;
    }

    public CarOrder(CarOrder carOrder) {
        this.user = carOrder.user;
        this.manger = carOrder.manger;
        this.parts = carOrder.parts.stream().toList();
        this.modelId = carOrder.modelId;

        this.orderType = carOrder.orderType;

        this.state = carOrder.state;
    }

    public void SetState (OrderState state) {
        this.state = state;
    }
}
