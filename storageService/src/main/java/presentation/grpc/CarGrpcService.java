package presentation.grpc;
import application.contracts.dataobjects.cars.CheckAvailableForTestDriven;
import application.contracts.dataobjects.cars.CheckPossibilityToCreateAnOrder;
import application.contracts.dataobjects.cars.GetAllCars;
import application.contracts.dataobjects.cars.GetCarById;
import application.services.CarService;
import application.services.exceptions.ConflictException;
import application.services.exceptions.NotFoundException;
import domain.valueObjects.Color;
import domain.valueObjects.ModelName;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.svyatniy.common.domain.proto.*;

import java.util.UUID;

@GrpcService
public class CarGrpcService extends CarsServiceGrpc.CarsServiceImplBase {

    private final CarService carService;

    public CarGrpcService(CarService carService) {
        this.carService = carService;
    }

    private CarInfo toProtoCarInfo(application.contracts.dataobjects.cars.CarInfo carInfo) {
        return CarInfo
                .newBuilder()
                .setId(carInfo.id().toString())
                .setModelName(carInfo.modelName())
                .setBrandName(carInfo.brandName())
                .setColor(carInfo.color())
                .setPrice(carInfo.price())
                .setGearBoxType(carInfo.gearBoxType())
                .setCarCase(carInfo.carCase())
                .setRudderInfo(
                        PartInfo.newBuilder()
                                .setId(carInfo.rudderInfo().id().toString())
                                .setName(carInfo.rudderInfo().name())
                                .setPartType(carInfo.rudderInfo().partType())
                                .build()
                )
                .setWheelInfo(
                        PartInfo.newBuilder()
                                .setId(carInfo.wheelInfo().id().toString())
                                .setName(carInfo.wheelInfo().name())
                                .setPartType(carInfo.wheelInfo().partType())
                                .build()
                )
                .setTransmissionInfo(
                        PartInfo.newBuilder()
                                .setId(carInfo.transmissionInfo().id().toString())
                                .setName(carInfo.transmissionInfo().name())
                                .setPartType(carInfo.transmissionInfo().partType())
                                .build()
                )
                .setInteriorInfo(
                        PartInfo.newBuilder()
                                .setId(carInfo.interiorInfo().id().toString())
                                .setName(carInfo.interiorInfo().name())
                                .setPartType(carInfo.interiorInfo().partType())
                                .build()
                )
                .setWheelDrive(carInfo.wheelDrive())
                .setEngineInfo(
                        PartInfo.newBuilder()
                                .setId(carInfo.engineInfo().id().toString())
                                .setName(carInfo.engineInfo().name())
                                .setPartType(carInfo.engineInfo().partType())
                                .build()
                )
                .setEnginePower(carInfo.enginePower())
                .setEngineVolume(carInfo.engineVolume())
                .setEngineType(carInfo.engineType())
                .build();
    }

    @Override
    public void checkCarConfig (CheckCarConfigRequest request,
                                StreamObserver<CheckCarConfigResponse> responseObserver) {

        var serviceRequest = CheckPossibilityToCreateAnOrder.Request.builder()
                .modelName(new ModelName(request.getModelName()))
                .color(new Color(request.getColor()))
                .rudderId(request.getRudderId().isEmpty() ? null : UUID.fromString(request.getRudderId()))
                .wheelId(request.getWheelId().isEmpty() ? null : UUID.fromString(request.getWheelId()))
                .transmissionId(request.getTransmissionId().isEmpty() ? null : UUID.fromString(request.getTransmissionId()))
                .interiorId(request.getInteriorId().isEmpty() ? null : UUID.fromString(request.getInteriorId()))
                .engineId(request.getEngineId().isEmpty() ? null : UUID.fromString(request.getEngineId()))
                .build();

        try {
            var serviceResponse = carService.CheckPossibilityToCreateAnOrder(serviceRequest);

            var response = CheckCarConfigResponse.newBuilder()
                    .setModelId(serviceResponse.modelId().toString())

                    .addAllParts(serviceResponse.parts().stream().map(
                            x -> PartEntry
                                    .newBuilder()
                                    .setPartName(x.getFirst())
                                    .setPartId(x.getSecond().toString())
                                    .build()
                    ).toList())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (ConflictException e) {
            responseObserver.onError(Status.ABORTED
                    .withDescription("conflict of configuration")
                    .withCause(e)
                    .asException()
            );
        }
        catch (NotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("some of parts is unreachable")
                    .withCause(e)
                    .asException()
            );
        }
    }

    @Override
    public void getAllCars(GetAllCarsRequest request,
                           StreamObserver<GetAllCarsResponse> responseObserver) {
        var serviceRequest = GetAllCars.Request.builder()
                .build();

        try {
            var serviceResponse = carService.GetAllCars(serviceRequest);

            var response = GetAllCarsResponse.newBuilder()
                    .addAllCars(
                            serviceResponse.models().stream()
                                    .map(
                                        this::toProtoCarInfo
                                    )
                                    .toList()
                    )
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (NotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("no such carid")
                    .withCause(e)
                    .asException()
            );
        }
    }

    @Override
    public void getCarById (GetCarByIdRequest request,
                            StreamObserver<GetCarByIdResponse> responseObserver) {
        var serviceRequest = GetCarById.Request.builder()
                .carId(UUID.fromString(request.getCarId()))
                .build();

        try {
            var serviceResponse = carService.GetCarById(serviceRequest);

            var response = GetCarByIdResponse.newBuilder()
                    .setCar(
                            this.toProtoCarInfo(serviceResponse.carInfo())
                    )
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (NotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("no such carid")
                    .withCause(e)
                    .asException()
            );
        }
    }

    @Override
    public void checkAvalForTestDriven (CheckAvalForTestDrivenRequest request,
                            StreamObserver<CheckAvalForTestDrivenResponse> responseObserver) {
        var serviceRequest = CheckAvailableForTestDriven.Request.builder()
                        .modelId(UUID.fromString(request.getId()))
                        .build();

        try {
            var serviceResponse = carService.CheckAvailableForTestDriven(serviceRequest);

            var response = CheckAvalForTestDrivenResponse.newBuilder()
                    .setId(serviceResponse.modelId().toString())
                    .setIsAvailable(serviceResponse.isAvailable())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (NotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("cannot find car for test drive")
                    .withCause(e)
                    .asException()
            );
        }
    }
}
