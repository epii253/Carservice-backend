package presentation.mappers.carparts;

import application.contracts.dataobjects.carparts.GetAllPartsRequest;
import org.mapstruct.Mapper;
import presentation.entryobjects.carparts.GetAllPartsDto;

@Mapper(componentModel = "spring")
public interface IGetAllPartsMapper {
    GetAllPartsRequest toRequest(GetAllPartsDto dto);
}
