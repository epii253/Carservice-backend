package application.services;

import application.contracts.dataobjects.cars.*;
import application.contracts.mappers.cars.ICarMapper;
import application.contracts.ports.ICarService;
import application.outbox.IKafkaPublisher;
import application.outbox.ToPublishEvent;
import application.outbox.payloads.CarOrderEventPayload;
import application.outbox.payloads.CarOrderRequestPayload;
import application.outbox.payloads.EventPayload;
import application.outbox.publishers.KafkaRegularPublisher;
import application.repositories.IBuildOrdersRepo;
import application.repositories.ICarModelsRepo;
import application.repositories.IRequiredPartRepo;
import application.repositories.parts.*;
import application.repositories.rows.RequiredPartStock;
import application.services.clients.KeycloakRestClientConfig;
import application.services.exceptions.ConflictException;
import application.services.exceptions.NotFoundException;
import application.services.exceptions.UnauthorizedException;
import application.services.security.SecurityExtractor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.entities.*;
import domain.services.carBuilder.CarBuilder;
import domain.utilityentities.carfilter.CarFilter;
import domain.valueObjects.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.*;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class CarService implements ICarService {
    // Security
    private final SecurityExtractor securityExtractor;

    // Services
    private final KeycloakService keycloakService;

    //Mappers
    private final ICarMapper carMapper;

    private final ObjectMapper objectMapper;

    // Repositories
    private final IBuildOrdersRepo buildOrdersRepo;
    private final ICarModelsRepo carModelsRepo;
    private final IRudderRepo rudderRepo;
    private final IWheelRepo wheelRepo;
    private final ITransmissionRepo transmissionRepo;
    private final IInteriorRepo interiorRepo;
    private final IEngineRepo engineRepo;

    private final IRequiredPartRepo requiredPartRepo;

    // External Events
    private final KafkaRegularPublisher kafkaRegularPublisher;

    // Utilities
    private final Random rand;


    public CarService(
            SecurityExtractor securityExtractor, KeycloakService keycloakService,
            ICarMapper carMapper,
            ObjectMapper objectMapper,
            IBuildOrdersRepo buildOrdersRepo,
            ICarModelsRepo carModelsRepo,
            IRudderRepo rudderRepo,
            IWheelRepo wheelRepo,
            ITransmissionRepo transmissionRepo,
            IInteriorRepo interiorRepo,
            IEngineRepo engineRepo,
            IRequiredPartRepo requiredPartRepo, KafkaRegularPublisher kafkaRegularPublisher
    ) {
        this.securityExtractor = securityExtractor;
        this.keycloakService = keycloakService;
        this.carMapper = carMapper;
        this.objectMapper = objectMapper;
        this.buildOrdersRepo = buildOrdersRepo;
        this.carModelsRepo = carModelsRepo;
        this.rudderRepo = rudderRepo;
        this.wheelRepo = wheelRepo;
        this.transmissionRepo = transmissionRepo;
        this.interiorRepo = interiorRepo;
        this.engineRepo = engineRepo;
        this.requiredPartRepo = requiredPartRepo;
        this.kafkaRegularPublisher = kafkaRegularPublisher;
        this.rand = new Random();

    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER', 'STORAGE_ADMIN', 'USER')")
    public GetCertainCar.Response GetCertainCar(GetCertainCar.Request request) {
        return carMapper.toGetCertainCarResponse(
                carModelsRepo
                        .findAll(CarFilter.hasModelName(request.modelName()))
                        .stream()
                        .toList()
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER', 'STORAGE_ADMIN')")
    public NewCar.Response AddNewCarModel(NewCar.Request request) {
        var builder = CarModel.builder();

        for (var entry : request.requiredParts()) {
            builder.requiredPart(entry);
        }
        builder.modelName(new ModelName(request.modelName()));
        builder.carCase(new CarCase(request.carCase()));
        builder.brandName(new BrandName(request.brandName()));

        builder.wheeldrive(request.wheeldrive());
        builder.color(new Color(request.color()));
        builder.initialPrice(new Price(request.initialPrice()));
        builder.gearBoxType(request.gearBoxType());

        if (!rudderRepo.existsById(request.rudderId())
            || !wheelRepo.existsById(request.wheelId())
            || !transmissionRepo.existsById(request.transmissionId())
            || !interiorRepo.existsById(request.interiorId())
            || !engineRepo.existsById(request.engineId()) ) {

            throw new NotFoundException("no detail");
        }

        builder.rudder(rudderRepo.findById(request.rudderId()).get());
        builder.wheel(wheelRepo.findById(request.wheelId()).get());
        builder.transmission(transmissionRepo.findById(request.transmissionId()).get());
        builder.interior(interiorRepo.findById(request.interiorId()).get());
        builder.engine(engineRepo.findById(request.engineId()).get());

        var model = builder.build();
        model = carModelsRepo.save(model);

        for (var entry : request.requiredParts()) {
            requiredPartRepo.save(new RequiredPartStock(model, entry));
        }

        return carMapper.toAddNewCarResponse(model);
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER', 'STORAGE_ADMIN', 'USER')")
    public GetAllCars.Response GetAllCars(GetAllCars.Request request) {
        return carMapper.toGetAllCarsResponse(carModelsRepo.findAll());
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER', 'STORAGE_ADMIN', 'USER')")
    public GetCarsBy.Response GetWithFilter(GetCarsBy.Request request) {
        return carMapper.toGetCarsByResponse(carModelsRepo.findAll(
                        Specification
                            .where(CarFilter.hasPriceLowerBound(request.priceLowerBound()))
                            .and(CarFilter.hasPriceUpperBound(request.priceUpperBound()))
                            .and(CarFilter.hasBrandName(request.brandName()))
                            .and(CarFilter.hasModelName(request.modelName()))
                            .and(CarFilter.hasCarCase(request.carCase()))
                            .and(CarFilter.hasEngineType(request.engineType()))
                            .and(CarFilter.hasEnginePowerLowerBound(request.enginePowerLowerBound()))
                            .and(CarFilter.hasEnginePowerUpperBound(request.enginePowerUpperBound()))
                            .and(CarFilter.hasEngineVolumeLowerBound(request.engineVolumeLowerBound()))
                            .and(CarFilter.hasEngineVolumeUpperBound(request.engineVolumeUpperBound()))
                            .and(CarFilter.hasGearBoxType(request.gearBoxType()))
                            .and(CarFilter.hasWheelDrive(request.wheelDrive()))
                            .and(CarFilter.hasColor(request.color()))
        ).stream().toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER', 'USER')")
    public GetCarById.Response GetCarById(GetCarById.Request request) {
        var model = carModelsRepo.findById(request.carId());

        if (model.isEmpty()) {
            throw new NotFoundException("no");
        }

        return carMapper.toGetCarByIdResponse(
                model.get()
        );
    }


    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','STORAGE_ADMIN', 'ORDER_SERVICE', 'USER', 'MANAGER')")
    public CheckPossibilityToCreateAnOrder.Response CheckPossibilityToCreateAnOrder(CheckPossibilityToCreateAnOrder.Request request) {
        var carModel = QCarModel.carModel;
        var allModels = carModelsRepo.findAll(
                Specification
                        .where(CarFilter.hasModelName(request.modelName()))
                        .and(CarFilter.hasColor(request.color()))
        ).stream().toList();

        if (allModels.isEmpty()) {
            throw new ConflictException("no moel");
        }

        var model = allModels.getFirst();

        var defaultParts = new TreeMap<PartType, CarPart>();
        defaultParts.put(model.getRudder().getPartType(), model.getRudder());
        defaultParts.put(model.getWheels().getPartType(), model.getWheels());
        defaultParts.put(model.getTransmission().getPartType(), model.getTransmission());
        defaultParts.put(model.getInterior().getPartType(), model.getInterior());
        defaultParts.put(model.getEngine().getPartType(), model.getEngine());

        var builder = CarBuilder.Create(model, defaultParts);

        if (request.rudderId() != null) {
            var rudderPart = rudderRepo.findById(request.rudderId());

            if (rudderPart.isEmpty()) {
                throw new NotFoundException(null);
            }
            builder.WithPart(rudderPart.get(), rudderPart.get().getDiffPrice());
        }

        if (request.wheelId() != null) {
            var wheelPart = wheelRepo.findById(request.wheelId());

            if (wheelPart.isEmpty()) {
                throw new NotFoundException(null);
            }
            builder.WithPart(wheelPart.get(), wheelPart.get().getDiffPrice());
        }

        if (request.transmissionId() != null) {
            var transmissionPart = transmissionRepo.findById(request.transmissionId());

            if (transmissionPart.isEmpty()) {
                throw new NotFoundException(null);
            }
            builder.WithPart(transmissionPart.get(), transmissionPart.get().getDiffPrice());
        }

        if (request.interiorId() != null) {
            var interiorPart = interiorRepo.findById(request.interiorId());

            if (interiorPart.isEmpty()) {
                throw new NotFoundException(null);
            }
            builder.WithPart(interiorPart.get(), interiorPart.get().getDiffPrice());
        }

        if (request.engineId() != null) {
            var enginePart = engineRepo.findById(request.engineId());

            if (enginePart.isEmpty()) {
                throw new NotFoundException(null);
            }
            builder.WithPart(enginePart.get(), enginePart.get().getDiffPrice());
        }
        var car = builder.Build();

        if (car == null) {
            throw new ConflictException("cannot");
        }

        return new CheckPossibilityToCreateAnOrder.Response(
                model.getId(),
                car.getParts().stream()
                        .map(x -> new Pair<>(x.getFirst().toString(), x.getSecond()))
                        .toList()
        );
    }

    @Override
    public CheckAvailableForTestDriven.Response CheckAvailableForTestDriven(CheckAvailableForTestDriven.Request request) {
        var carModel = carModelsRepo.findById(request.modelId());

        if (carModel.isEmpty()) {
            return new CheckAvailableForTestDriven.Response(request.modelId(), false);
        }

        return new CheckAvailableForTestDriven.Response(request.modelId(), true);
    }

    private void CreateBuildOrderExceptionShooter(CreateBuildOrder.Request request) {
        if (StreamSupport.stream(buildOrdersRepo
                        .findAll(QBuildOrder
                                .buildOrder
                                .sourceOrderId.eq(request.orderId())
                        ).spliterator(), false)
                .findFirst()
                .orElse(null) != null) {
            return;
        }

        var modelResult = carModelsRepo.findById(request.modelId());

        if (modelResult.isEmpty()) {
            throw new ConflictException("no model");
        }
        var model = modelResult.get();

        var defaultParts = new TreeMap<PartType, CarPart>();
        defaultParts.put(model.getRudder().getPartType(), model.getRudder());
        defaultParts.put(model.getWheels().getPartType(), model.getWheels());
        defaultParts.put(model.getTransmission().getPartType(), model.getTransmission());
        defaultParts.put(model.getInterior().getPartType(), model.getInterior());
        defaultParts.put(model.getEngine().getPartType(), model.getEngine());

        var builder = CarBuilder.Create(model, defaultParts);

        var allParts = request.parts();

        // Add rudder
        var rudderInfo = allParts.stream()
                .filter(x -> Objects.equals(x.getFirst(), PartType.Rudder.toString()))
                .toList()
                .getFirst();
        var rudderPart = rudderRepo.findById(rudderInfo.getSecond());

        if (rudderPart.isEmpty()) {
            throw new NotFoundException(null);
        }
        builder.WithPart(rudderPart.get(), rudderPart.get().getDiffPrice());
        //

        // Add wheel
        var wheelInfo = allParts.stream()
                .filter(x -> Objects.equals(x.getFirst(), PartType.Wheel.toString()))
                .toList()
                .getFirst();
        var wheelPart = wheelRepo.findById(wheelInfo.getSecond());

        if (wheelPart.isEmpty()) {
            throw new NotFoundException(null);
        }
        builder.WithPart(wheelPart.get(), wheelPart.get().getDiffPrice());
        //

        // Add transmission
        var transmissionInfo = allParts.stream()
                .filter(x -> Objects.equals(x.getFirst(), PartType.Transmission.toString()))
                .toList()
                .getFirst();
        var transmissionPart = transmissionRepo.findById(transmissionInfo.getSecond());

        if (transmissionPart.isEmpty()) {
            throw new NotFoundException(null);
        }
        builder.WithPart(transmissionPart.get(), transmissionPart.get().getDiffPrice());
        //

        // Add interior
        var interiorInfo = allParts.stream()
                .filter(x -> Objects.equals(x.getFirst(), PartType.Interior.toString()))
                .toList()
                .getFirst();
        var interiorPart = interiorRepo.findById(interiorInfo.getSecond());

        if (interiorPart.isEmpty()) {
            throw new NotFoundException(null);
        }
        builder.WithPart(interiorPart.get(), interiorPart.get().getDiffPrice());
        //

        // Add engine
        var engineInfo = allParts.stream()
                .filter(x -> Objects.equals(x.getFirst(), PartType.Engine.toString()))
                .toList()
                .getFirst();
        var enginePart = engineRepo.findById(engineInfo.getSecond());

        if (enginePart.isEmpty()) {
            throw new NotFoundException(null);
        }
        builder.WithPart(enginePart.get(), enginePart.get().getDiffPrice());
        //

        var car = builder.Build();

        if (car == null) {
            makeEvent(
                    CarOrderRequestPayload.builder()
                            .orderId(request.orderId())
                            .build(),
                    Instant.now(),
                    "OrderRejected"
            );
            return;
        }

        var storageAdmins = keycloakService.getUsersByRole("STORAGE_ADMIN");

        if (storageAdmins.isEmpty()) {
            throw new ConflictException("Cannot do this");
        }

        var buildOrder = new BuildOrder(
                request.orderId(),
                request.orderType(),
                car,
                car.getParts().stream().map(Pair::getFirst).toList(),
                UUID.fromString(storageAdmins.get(rand.nextInt(storageAdmins.size())).id()),
                BuildOrderStatus.ASSEMBLED
        );

        buildOrdersRepo.save(buildOrder);
        makeEvent(
                CarOrderRequestPayload.builder()
                        .orderId(request.orderId())
                        .build(),
                Instant.now(),
                "OrderApproved"
        );
    }

    public void CreateBuildOrder(CreateBuildOrder.Request request) {
        try {
            CreateBuildOrderExceptionShooter(request);
        } catch (Exception e) {
            makeEvent(
                    CarOrderRequestPayload.builder()
                            .orderId(request.orderId())
                            .build(),
                    Instant.now(),
                    "OrderRejected"
            );
        }
    }

    private void makeEvent(EventPayload payload, Instant now, String eventType) {
        var event = new ToPublishEvent(
                eventType,
                payload.toPayload(objectMapper)
        );
        kafkaRegularPublisher.publish(event);
    }
}
