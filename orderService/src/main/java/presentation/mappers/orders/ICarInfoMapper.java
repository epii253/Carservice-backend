package presentation.mappers.orders;

import application.contracts.dataobjects.cars.CarInfo;
import application.contracts.dataobjects.cars.PartInfo;
import org.mapstruct.Mapper;
import presentation.entryobjects.orders.CarInfoResponse;
import presentation.entryobjects.orders.PartInfoResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ICarInfoMapper {
    CarInfoResponse toResponse(CarInfo carInfo);
    PartInfoResponse toResponse(PartInfo partInfo);
    List<CarInfoResponse> toResponseList(List<CarInfo> carInfos);
}
