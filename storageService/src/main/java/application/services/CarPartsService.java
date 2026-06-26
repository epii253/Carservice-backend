package application.services;

import application.contracts.dataobjects.carparts.*;
import application.contracts.mappers.carparts.ICarPartResponseMapper;
import application.contracts.mappers.carparts.IGetAllPartsAppMapper;
import application.contracts.ports.ICarPartService;
import application.repositories.parts.*;
import application.repositories.parts.stocks.*;
import application.services.exceptions.UnauthorizedException;
import application.services.security.SecurityExtractor;
import domain.entities.CarPart;
import domain.entities.parts.*;
import domain.valueObjects.PartType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Stream;

@Service
public class CarPartsService implements ICarPartService {
    // Security
    private final SecurityExtractor securityExtractor;

    // Mappers
    private final IGetAllPartsAppMapper getAllPartsMapper;

    private final ICarPartResponseMapper carPartResponseMapper;
    //  repos
    private final IEngineRepo engineRepo;
    private final IEngineStockRepo engineStockRepo;
    private final IGearBoxRepo gearBoxRepo;
    private final IGearBoxStockRepo gearBoxStockRepo;

    private final IInteriorRepo interiorRepo;
    private final IInteriorStockRepo interiorStockRepo;

    private final IRudderRepo rudderRepo;
    private final IRudderStockRepo rudderStockRepo;

    private final ITransmissionRepo transmissionRepo;
    private final ITransmissionStockRepo transmissionStockRepo;

    private final IWheelRepo wheelRepo;
    private final IWheelStockRepo wheelStockRepo;


    public CarPartsService(
            SecurityExtractor securityExtractor, IGetAllPartsAppMapper getAllPartsMapper,
            ICarPartResponseMapper carPartResponseMapper,
            IEngineRepo engineRepo,
            IEngineStockRepo engineStockRepo,
            IGearBoxRepo gearBoxRepo,
            IGearBoxStockRepo gearBoxStockRepo,
            IInteriorRepo interiorRepo,
            IInteriorStockRepo interiorStockRepo,
            IRudderRepo rudderRepo,
            IRudderStockRepo rudderStockRepo,
            ITransmissionRepo transmissionRepo,
            ITransmissionStockRepo transmissionStockRepo,
            IWheelRepo wheelRepo,
            IWheelStockRepo wheelStockRepo
    ) {
        this.securityExtractor = securityExtractor;
        this.getAllPartsMapper = getAllPartsMapper;
        this.carPartResponseMapper = carPartResponseMapper;
        this.engineRepo = engineRepo;
        this.engineStockRepo = engineStockRepo;
        this.gearBoxRepo = gearBoxRepo;
        this.gearBoxStockRepo = gearBoxStockRepo;
        this.interiorRepo = interiorRepo;
        this.interiorStockRepo = interiorStockRepo;
        this.rudderRepo = rudderRepo;
        this.rudderStockRepo = rudderStockRepo;
        this.transmissionRepo = transmissionRepo;
        this.transmissionStockRepo = transmissionStockRepo;
        this.wheelRepo = wheelRepo;
        this.wheelStockRepo = wheelStockRepo;
    }

    @Override
    @Transactional
    public GetAllPartsResponse GetAllParts(GetAllPartsRequest request)
        throws UnauthorizedException {
        Stream<CarPart> parts = engineRepo.findAll().stream().map(Engine.class::cast);
        parts = Stream.concat(parts, gearBoxRepo.findAll().stream().map(CarPart.class::cast));
        parts = Stream.concat(parts, interiorRepo.findAll().stream().map(CarPart.class::cast));
        parts = Stream.concat(parts, rudderRepo.findAll().stream().map(CarPart.class::cast));
        parts = Stream.concat(parts, transmissionRepo.findAll().stream().map(CarPart.class::cast));
        parts = Stream.concat(parts, wheelRepo.findAll().stream().map(CarPart.class::cast));

        return getAllPartsMapper.toResponse(parts.toList());
    }

    @Override
    @Transactional
    public AddCarPartResponse AddRuderPart(AddRudderRequest request) {

        var part = new Rudder(PartType.Rudder,
                request.name(),
                request.diffPrice(),
                request.compatibleModels());

        part = rudderRepo.save(part);
        rudderStockRepo.save(new RudderStock(part));

        return carPartResponseMapper.toResponse(part);
    }

    @Override
    @Transactional
    public AddCarPartResponse AddWheelPart(AddWheelRequest request) {
        var part = new Wheel(PartType.Wheel,
                request.name(),
                request.diffPrice(),
                request.compatibleModels()
        );

        part = wheelRepo.save(part);
        wheelStockRepo.save(new WheelStock(part));

        return carPartResponseMapper.toResponse(part);
    }
    @Override
    @Transactional
    public AddCarPartResponse AddTransmissionPart(AddTransmissionRequest request) {

        var part = new Transmission(PartType.Transmission,
                request.name(),
                request.diffPrice(),
                request.compatibleModels()
        );

        part = transmissionRepo.save(part);
        transmissionStockRepo.save(new TransmissionStock(part));

        return carPartResponseMapper.toResponse(part);
    }

    @Override
    @Transactional
    public AddCarPartResponse AddInteriorPart(
            AddInteriorRequest request
    ) {
        var part = new Interior(PartType.Interior,
                request.name(),
                request.diffPrice(),
                request.compatibleModels()
        );

        part = interiorRepo.save(part);
        interiorStockRepo.save(new InteriorStock(part));

        return carPartResponseMapper.toResponse(part);
    }

    @Override
    @Transactional
    public AddCarPartResponse AddEnginePart(AddEngineRequest request) {

        var part = new Engine(PartType.Engine,
                request.name(),
                request.diffPrice(),
                request.compatibleModels(),
                request.power(),
                request.volume(),
                request.engineType()
        );

        part = engineRepo.save(part);
        engineStockRepo.save(new EngineStock(part));

        return carPartResponseMapper.toResponse(part);
    }
}
