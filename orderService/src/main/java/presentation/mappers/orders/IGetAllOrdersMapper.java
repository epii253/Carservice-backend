package presentation.mappers.orders;

import application.contracts.dataobjects.orders.GetAllOrders;
import org.mapstruct.Mapper;
import presentation.entryobjects.orders.GetAllOrdersDto;
import presentation.mappers.VoMapper;

@Mapper(
        componentModel = "spring",
        uses = {VoMapper.class}
)
public interface IGetAllOrdersMapper {
    GetAllOrders.Request toRequest(GetAllOrdersDto dto);
}
