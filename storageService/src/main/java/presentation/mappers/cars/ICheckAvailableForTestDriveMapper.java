package presentation.mappers.cars;

import application.contracts.dataobjects.cars.CheckAvailableForTestDriven;
import application.contracts.dataobjects.cars.CheckPossibilityToCreateAnOrder;
import org.mapstruct.Mapper;
import presentation.entryobjects.cars.CheckAvailableForTestDriveDto;
import presentation.entryobjects.cars.CheckPossibilityToCreateAnOrderDto;
import presentation.mappers.VoMapper;

@Mapper(
        componentModel = "spring",
        uses = {VoMapper.class}
)
public interface ICheckAvailableForTestDriveMapper {
    CheckAvailableForTestDriven.Request toRequest(CheckAvailableForTestDriveDto dto);

}
