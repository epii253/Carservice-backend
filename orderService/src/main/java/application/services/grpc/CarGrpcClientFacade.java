package application.services.grpc;

import application.contracts.dataobjects.cars.CarConfigResult;
import application.contracts.dataobjects.cars.CarInfo;
import application.contracts.dataobjects.cars.TestDriveAvailability;
import domain.valueObjects.Color;
import domain.valueObjects.ModelName;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.svyatniy.common.domain.proto.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CarGrpcClientFacade {

    @GrpcClient("cars-service")
    private CarsServiceGrpc.CarsServiceBlockingStub carsServiceBlockingStub;

    private final CarGrpcMapper mapper;

    public CarConfigResult checkCarConfig(
        @NonNull ModelName modelName,
        @NonNull Color color,
        UUID rudderId,
        UUID wheelId,
        UUID transmissionId,
        UUID interiorId,
        UUID engineId
    ) {
        return mapper.toCarConfigResult(
                carsServiceBlockingStub
                        .withDeadlineAfter(5, TimeUnit.SECONDS)
                        .checkCarConfig(CheckCarConfigRequest.newBuilder()
                                .setModelName(modelName.getName())
                                .setColor(color.getName())
                                .setRudderId(rudderId != null ? rudderId.toString() : "")
                                .setWheelId(wheelId != null ? wheelId.toString() : "")
                                .setTransmissionId(transmissionId != null ? transmissionId.toString() : "")
                                .setInteriorId(interiorId != null ? interiorId.toString() : "")
                                .setEngineId(engineId != null ? engineId.toString() : "")
                                .build()));
    }

    public List<CarInfo> getAllCars() {
        return mapper.toCarInfoList(
                carsServiceBlockingStub
                        .withDeadlineAfter(5, TimeUnit.SECONDS)
                        .getAllCars(GetAllCarsRequest.newBuilder().build())
                        .getCarsList());
    }

    public CarInfo getCarById(@NonNull UUID carId) {
        return mapper.toCarInfo(
                carsServiceBlockingStub
                        .withDeadlineAfter(5, TimeUnit.SECONDS)
                        .getCarById(GetCarByIdRequest.newBuilder()
                                .setCarId(carId.toString())
                                .build())
                        .getCar());
    }

    public TestDriveAvailability checkAvalForTestDriven(@NonNull UUID modelId) {
        return mapper.toTestDriveAvailability(
                carsServiceBlockingStub
                        .withDeadlineAfter(5, TimeUnit.SECONDS)
                        .checkAvalForTestDriven(CheckAvalForTestDrivenRequest.newBuilder()
                                .setId(modelId.toString())
                                .build()));
    }
}