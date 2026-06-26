package presentation.mappers.cars;

import application.contracts.dataobjects.cars.GetCertainCar;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import presentation.entryobjects.cars.GetCertainCarDto;
import presentation.mappers.VoMapper;

@Mapper(
    componentModel = "spring",
        uses = {VoMapper.class}
)
public interface IGetCarsMapper {
    @Mapping(target = "modelName", source = "modelName")
    GetCertainCar.Request toRequest(GetCertainCarDto dto);
}
