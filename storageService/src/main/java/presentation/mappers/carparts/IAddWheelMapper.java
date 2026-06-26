package presentation.mappers.carparts;

import application.contracts.dataobjects.carparts.AddWheelRequest;
import domain.valueObjects.ModelName;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import presentation.entryobjects.carparts.AddWheelDto;
import presentation.mappers.VoMapper;

import java.util.List;

@Mapper(
    componentModel = "spring",
    uses = {VoMapper.class}
)
public interface IAddWheelMapper {
    @Mapping(target = "diffPrice", source = "diffPrice")
    @Mapping(target = "compatibleModels", source = "compatibleModels", qualifiedByName = "toModelNames")
    AddWheelRequest toRequest(AddWheelDto source);

    @Named("toModelNames")
    default List<ModelName> mapModels(List<String> models) {
        return models.stream().map(ModelName::new).toList();
    }
}
