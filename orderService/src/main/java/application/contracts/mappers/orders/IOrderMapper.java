package application.contracts.mappers.orders;

import application.contracts.dataobjects.orders.DoCustomOrder;
import application.contracts.dataobjects.orders.DoOrder;
import application.contracts.dataobjects.orders.GetAllOrders;
import application.contracts.dataobjects.orders.OrderInfo;
import com.fasterxml.jackson.databind.JsonNode;
import domain.entities.CarOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IOrderMapper {
    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "manger.id", target = "managerId")
    DoOrder.Response toOrderResponse(CarOrder order);

    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "manger.id", target = "managerId")
    DoCustomOrder.Response toCustomOrderResponse(CarOrder order);

    default GetAllOrders.Response toAllOrdersResponse(List<CarOrder> orders) {
        return new GetAllOrders.Response(toOrderInfoList(orders));
    }
    List<OrderInfo> toOrderInfoList(List<CarOrder> parts);

    //
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "manger.id", target = "managerId")

    @Mapping(source = "modelId", target = "modelId")
    @Mapping(source = "parts", target = "parts")

    @Mapping(source = "orderType", target = "orderType", qualifiedByName = "enumToString")
    @Mapping(source = "state", target = "orderState", qualifiedByName = "enumToString")
    OrderInfo toOrderInfo(CarOrder order);

    @Named("jsonNodeToString")
    default String nodeToString(JsonNode node) {
        return (node == null) ? null : node.toString();
    }

    @Named("enumToString")
    default String enumToString(Enum<?> e) {
        return e == null ? null : e.name();
    }
}
