package presentation.mappers.orders;

import application.contracts.dataobjects.orders.GetAllOrders;
import application.contracts.dataobjects.orders.MoveFrowardOrder;
import org.mapstruct.Mapper;
import presentation.entryobjects.orders.GetAllOrdersDto;
import presentation.entryobjects.orders.MoveForwardOrderDto;
import presentation.mappers.VoMapper;

@Mapper(
        componentModel = "spring",
        uses = {VoMapper.class}
)
public interface IMoveForwardOrderMapper {
    MoveFrowardOrder.Request toRequest(MoveForwardOrderDto dto);

}
