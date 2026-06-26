package domain.entities;

public interface IOrderState {
    boolean Next(CarOrder context);
    boolean Cancel(CarOrder context);
}
