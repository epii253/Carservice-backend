package application.contracts.mappers.carparts;

import application.contracts.dataobjects.carparts.AddCarPartResponse;
import domain.entities.CarPart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ICarPartResponseMapper {
    @Mapping(source = "id", target = "partId")
    AddCarPartResponse toResponse(CarPart carPart);
}
