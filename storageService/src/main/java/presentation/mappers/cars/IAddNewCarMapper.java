package presentation.mappers.cars;

import application.contracts.dataobjects.cars.NewCar;
import org.mapstruct.Mapper;
import presentation.entryobjects.cars.AddNewCarDto;
import presentation.mappers.VoMapper;

@Mapper(
        componentModel = "spring",
        uses = {VoMapper.class}
)
public interface IAddNewCarMapper {
    NewCar.Request toRequest(AddNewCarDto dto);
}
