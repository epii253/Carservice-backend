package application.contracts.dataobjects.orders;

import java.util.List;


public class GetAllOrders {
    private GetAllOrders() {}
    public record Request() {};

    public record Response(List<OrderInfo> infoList) {};
}
