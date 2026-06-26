package domain.entities;

import domain.valueObjects.OrderType;
import domain.valueObjects.UserId;
import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class AbstractOrderState {
    private final UserId id;

    private final UserId mangerId;

    private final UUID carId;

    private final OrderType orderType;

    protected AbstractOrderState(UserId id, UserId mangerId, UUID carId, OrderType orderType) {
        this.id = id;
        this.mangerId = mangerId;
        this.carId = carId;
        this.orderType = orderType;
    }

    public abstract boolean TryNextState();

    public boolean TryCancel() {
        return false;
    }

    public abstract String ToString();
}
