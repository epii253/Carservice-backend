package application.contracts.mappers.cars;

import application.contracts.dataobjects.cars.*;
import application.contracts.mappers.carparts.IGetAllPartsAppMapper;
import domain.entities.CarModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
    componentModel = "spring",
    uses = {IGetAllPartsAppMapper.class}
)
public interface ICarMapper {
    default GetCertainCar.Response toGetCertainCarResponse(List<CarModel> models) {
        return new GetCertainCar.Response(toCarInfoList(models));
    }

    default NewCar.Response toAddNewCarResponse(CarModel model) {
        return new NewCar.Response(toCarInfo(model));
    }

    default GetAllCars.Response toGetAllCarsResponse(List<CarModel> models) {
        return new GetAllCars.Response(toCarInfoList(models));
    }

    default GetCarById.Response toGetCarByIdResponse(CarModel carModel) {
        return new GetCarById.Response(toCarInfo(carModel));
    }

    default GetCarsBy.Response toGetCarsByResponse(List<CarModel> models) {
        return new GetCarsBy.Response(toCarInfoList(models));
    }
    List<CarInfo> toCarInfoList(List<CarModel> models);

    @Mapping(source = "modelName.name", target = "modelName")
    @Mapping(source = "brandName.name", target = "brandName")
    @Mapping(source = "color.name", target = "color")
    @Mapping(source = "price.value", target = "price")
    @Mapping(source = "carCase.name", target = "carCase")

    @Mapping(source = "rudder", target = "rudderInfo")
    @Mapping(source = "wheels", target = "wheelInfo")
    @Mapping(source = "transmission", target = "transmissionInfo")
    @Mapping(source = "interior", target = "interiorInfo")
    @Mapping(source = "engine", target = "engineInfo")

    @Mapping(source = "engine.power", target = "enginePower")
    @Mapping(source = "engine.volume", target = "engineVolume")
    @Mapping(source = "engine.engineType", target = "engineType")

    @Mapping(source = "wheeldrive", target = "wheelDrive")
    CarInfo toCarInfo(CarModel carModel);
}
