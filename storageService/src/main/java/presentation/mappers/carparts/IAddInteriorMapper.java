package presentation.mappers.carparts;

import application.contracts.dataobjects.carparts.AddInteriorRequest;
import domain.valueObjects.ModelName;
import domain.valueObjects.Price;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import presentation.entryobjects.carparts.AddInteriorDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IAddInteriorMapper {
    @Mapping(target = "diffPrice", source = "diffPrice", qualifiedByName = "toPrice")
    @Mapping(target = "compatibleModels", source = "compatibleModels", qualifiedByName = "toModelNames")
    AddInteriorRequest toRequest(AddInteriorDto source);

    @Named("toPrice")
    default Price mapPrice(Float value) {
        return new Price(value);
    }

    @Named("toModelNames")
    default List<ModelName> mapModels(List<String> models) {
        return models.stream().map(ModelName::new).toList();
    }
}
