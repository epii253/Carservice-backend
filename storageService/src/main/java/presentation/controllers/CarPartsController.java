package presentation.controllers;

import application.contracts.dataobjects.carparts.AddCarPartResponse;
import application.contracts.dataobjects.carparts.GetAllPartsResponse;
import application.contracts.ports.ICarPartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import presentation.entryobjects.carparts.*;
import presentation.mappers.carparts.*;

@RestController
@RequestMapping("/parts")
public class CarPartsController {
    private final ICarPartService carPartService;

    private final IGetAllPartsMapper getAllPartsMapper;
    private final IAddRudderMapper addRudderMapper;
    private final IAddWheelMapper addWheelMapper;

    private final IAddTransmissionMapper addTransmissionMapper;

    private final IAddInteriorMapper addInteriorMapper;

    private final IAddEngineMapper addEngineMapper;

    public CarPartsController(
            ICarPartService carPartService,
            IGetAllPartsMapper getAllPartsMapper,
            IAddRudderMapper addRudderMapper,
            IAddWheelMapper addWheelMapper,
            IAddTransmissionMapper addTransmissionMapper,
            IAddInteriorMapper addInteriorMapper,
            IAddEngineMapper addEngineMapper
    ) {
        this.carPartService = carPartService;
        this.getAllPartsMapper = getAllPartsMapper;
        this.addRudderMapper = addRudderMapper;
        this.addWheelMapper = addWheelMapper;
        this.addTransmissionMapper = addTransmissionMapper;
        this.addInteriorMapper = addInteriorMapper;
        this.addEngineMapper = addEngineMapper;
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'STORAGE_ADMIN')")
    public ResponseEntity<GetAllPartsResponse> getAllParts(GetAllPartsDto request) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(carPartService.GetAllParts(getAllPartsMapper.toRequest(request)));
    }

    @PostMapping("/add_rudder")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'STORAGE_ADMIN')")
    public ResponseEntity<AddCarPartResponse> addRudder(@RequestBody AddRudderDto dto) {
        var response = carPartService.AddRuderPart(addRudderMapper.toRequest(dto));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @PostMapping("/add_wheel")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'STORAGE_ADMIN')")
    public ResponseEntity<AddCarPartResponse> addWheel(@RequestBody AddWheelDto dto) {
        var response = carPartService.AddWheelPart(addWheelMapper.toRequest(dto));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/add_transmission")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'STORAGE_ADMIN')")
    public ResponseEntity<AddCarPartResponse> addTransmission(@RequestBody AddTransmissionDto dto) {
        var response = carPartService.AddTransmissionPart(addTransmissionMapper.toRequest(dto));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/add_interior")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'STORAGE_ADMIN')")
    public ResponseEntity<AddCarPartResponse> addInterior(@RequestBody AddInteriorDto dto) {
        var response = carPartService.AddInteriorPart(addInteriorMapper.toRequest(dto));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/add_engine")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'STORAGE_ADMIN')")
    public ResponseEntity<AddCarPartResponse> addEngine(@RequestBody AddEngineDto dto) {
        var response = carPartService.AddEnginePart(addEngineMapper.toRequest(dto));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}