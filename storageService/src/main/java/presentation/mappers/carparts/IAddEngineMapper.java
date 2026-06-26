package presentation.mappers.carparts;

import application.contracts.dataobjects.carparts.AddEngineRequest;
import domain.valueObjects.ModelName;
import domain.valueObjects.Price;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import presentation.entryobjects.carparts.AddEngineDto;

@Mapper(componentModel = "spring")
public interface IAddEngineMapper {
    @Mapping(target = "diffPrice", source = "diffPrice", qualifiedByName = "toPrice")
    @Mapping(target = "compatibleModels", source = "compatibleModels")
    @Mapping(target = "power", source = "power")
    @Mapping(target = "volume", source = "volume")
    AddEngineRequest toRequest(AddEngineDto source);

    @Named("toPrice")
    default Price mapPrice(Float value) {
        return new Price(value);
    }

    default ModelName mapModelName(String value) {
        return new ModelName(value);
    }
}
