package application.contracts.mappers.carparts;

import application.contracts.dataobjects.carparts.GetAllPartsResponse;
import application.contracts.dataobjects.carparts.PartInfo;
import domain.entities.CarPart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IGetAllPartsAppMapper {
    default GetAllPartsResponse toResponse(List<CarPart> parts) {
        return new GetAllPartsResponse(toPartInfoList(parts));
    }
    List<PartInfo> toPartInfoList(List<CarPart> parts);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "partType", target = "partType")
    PartInfo toPartInfo(CarPart part);
}
