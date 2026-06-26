package presentation.mappers.cars;

import application.contracts.dataobjects.cars.CheckPossibilityToCreateAnOrder;
import application.contracts.dataobjects.cars.NewCar;
import org.mapstruct.Mapper;
import presentation.entryobjects.cars.AddNewCarDto;
import presentation.entryobjects.cars.CheckPossibilityToCreateAnOrderDto;
import presentation.mappers.VoMapper;

@Mapper(
        componentModel = "spring",
        uses = {VoMapper.class}
)
public interface ICheckPossibilityToCreateAnOrderMapper {
    CheckPossibilityToCreateAnOrder.Request toRequest(CheckPossibilityToCreateAnOrderDto dto);
}
