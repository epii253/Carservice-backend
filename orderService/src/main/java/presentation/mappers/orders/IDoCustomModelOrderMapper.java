package presentation.mappers.orders;

import application.contracts.dataobjects.orders.DoCustomOrder;
import org.mapstruct.Mapper;
import presentation.entryobjects.orders.DoCustomModelOrderDto;
import presentation.mappers.VoMapper;

@Mapper(
        componentModel = "spring",
        uses = {VoMapper.class}
)
public interface IDoCustomModelOrderMapper {
    DoCustomOrder.Request toRequest(DoCustomModelOrderDto dto);

}
