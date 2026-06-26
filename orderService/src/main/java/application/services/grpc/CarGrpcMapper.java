package application.services.grpc;

import application.contracts.dataobjects.cars.CarConfigResult;
import application.contracts.dataobjects.cars.CarInfo;
import application.contracts.dataobjects.cars.PartEntry;
import application.contracts.dataobjects.cars.PartInfo;
import application.contracts.dataobjects.cars.TestDriveAvailability;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.svyatniy.common.domain.proto.CheckAvalForTestDrivenResponse;
import ru.svyatniy.common.domain.proto.CheckCarConfigResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CarGrpcMapper {

    @Named("toPartInfo")
    default PartInfo toPartInfo(ru.svyatniy.common.domain.proto.PartInfo proto) {
        if (proto.getId().isEmpty()) return null;
        return new PartInfo(proto.getId(), proto.getName(), proto.getPartType());
    }

    @Mapping(target = "rudderInfo", source = "rudderInfo", qualifiedByName = "toPartInfo")
    @Mapping(target = "wheelInfo", source = "wheelInfo", qualifiedByName = "toPartInfo")
    @Mapping(target = "transmissionInfo", source = "transmissionInfo", qualifiedByName = "toPartInfo")
    @Mapping(target = "interiorInfo", source = "interiorInfo", qualifiedByName = "toPartInfo")
    @Mapping(target = "engineInfo", source = "engineInfo", qualifiedByName = "toPartInfo")
    CarInfo toCarInfo(ru.svyatniy.common.domain.proto.CarInfo proto);

    List<CarInfo> toCarInfoList(List<ru.svyatniy.common.domain.proto.CarInfo> protos);

    @Mapping(target = "partId", expression = "java(java.util.UUID.fromString(entry.getPartId()))")
    PartEntry toPartEntry(ru.svyatniy.common.domain.proto.PartEntry entry);

    @Mapping(target = "modelId", expression = "java(java.util.UUID.fromString(proto.getModelId()))")
    @Mapping(source = "partsList", target = "parts")
    CarConfigResult toCarConfigResult(CheckCarConfigResponse proto);

    @Mapping(target = "modelId", expression = "java(java.util.UUID.fromString(proto.getId()))")
    TestDriveAvailability toTestDriveAvailability(CheckAvalForTestDrivenResponse proto);
}
