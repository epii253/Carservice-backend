package presentation.mappers.orders;

import application.contracts.dataobjects.orders.DoOrder;
import org.mapstruct.Mapper;
import presentation.entryobjects.orders.DoModelOrderDto;
import presentation.mappers.VoMapper;

@Mapper(
        componentModel = "spring",
        uses = {VoMapper.class}
)
public interface IDoModelOrderMapper {
    DoOrder.Request toRequest(DoModelOrderDto dto);
}
