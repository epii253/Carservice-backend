package domain.entities;

import domain.valueObjects.OrderType;
import domain.valueObjects.UserId;
import lombok.Getter;
@Getter
public abstract class AbstractOrderState {
    private final UserId id;

    private final UserId mangerId;

    private final Car car;

    private final OrderType orderType;

    protected AbstractOrderState(UserId id, UserId mangerId, Car car, OrderType orderType) {
        this.id = id;
        this.mangerId = mangerId;
        this.car = car;
        this.orderType = orderType;
    }

    public abstract boolean TryNextState();

    public boolean TryCancel() {
        return false;
    }

    public abstract String ToString();
}
