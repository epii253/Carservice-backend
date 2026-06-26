package presentation.mappers.cars;

import application.contracts.dataobjects.cars.GetCarsBy;
import org.mapstruct.Mapper;
import presentation.entryobjects.cars.GetCarsByDto;
import presentation.mappers.VoMapper;

@Mapper(
        componentModel = "spring",
        uses = {VoMapper.class}
)
public interface IGetCarsByMapper {
    GetCarsBy.Request toRequest(GetCarsByDto dto);
}
