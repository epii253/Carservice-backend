package presentation.mappers.orders;

import application.contracts.dataobjects.orders.CanselOrder;
import org.mapstruct.Mapper;
import presentation.entryobjects.orders.CanselOrderDto;
import presentation.mappers.VoMapper;

@Mapper(
        componentModel = "spring",
        uses = {VoMapper.class}
)
public interface ICanselOrderMapper {
    CanselOrder.Request toRequest(CanselOrderDto dto);

}
