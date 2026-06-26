package presentation.controllers;

import application.contracts.dataobjects.cars.*;
import application.services.CarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import presentation.entryobjects.cars.*;
import presentation.mappers.cars.*;

@RestController
@RequestMapping("/cars")
public class CarController {

    private final ICheckPossibilityToCreateAnOrderMapper checkPossibilityToCreateAnOrderMapper;
    private final IGetCarsMapper carsMapper;

    private final ICheckAvailableForTestDriveMapper checkAvailableForTestDriveMapper;

    private final IAddNewCarMapper addNewCarMapper;
    private final IGetCarsByMapper getCarsByMapper;
    private final CarService carService;

    public CarController(
            ICheckPossibilityToCreateAnOrderMapper checkPossibilityToCreateAnOrderMapper, IGetCarsMapper carsMapper, ICheckAvailableForTestDriveMapper checkAvailableForTestDriveMapper,
            IAddNewCarMapper addNewCarMapper,
            IGetCarsByMapper getCarsByMapper,
            CarService carService
    ) {
        this.checkPossibilityToCreateAnOrderMapper = checkPossibilityToCreateAnOrderMapper;
        this.carsMapper = carsMapper;
        this.checkAvailableForTestDriveMapper = checkAvailableForTestDriveMapper;
        this.addNewCarMapper = addNewCarMapper;
        this.getCarsByMapper = getCarsByMapper;
        this.carService = carService;
    }

    @GetMapping("/{modelName}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER', 'STORAGE_ADMIN', 'USER')")
    public ResponseEntity<GetCertainCar.Response> GetCarByName(GetCertainCarDto dto) {
        var request = carsMapper.toRequest(dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(carService.GetCertainCar(request));
    }

    @PostMapping("/new")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER', 'STORAGE_ADMIN')")
    public ResponseEntity<NewCar.Response> PostNewCar(@RequestBody AddNewCarDto dto) {
        var request = addNewCarMapper.toRequest(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(carService.AddNewCarModel(request));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER', 'STORAGE_ADMIN', 'USER')")
    public ResponseEntity<GetAllCars.Response> GetAllCars() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(carService.GetAllCars(new GetAllCars.Request()));
    }

    @GetMapping("/find_by")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER', 'STORAGE_ADMIN', 'USER')")
    public ResponseEntity<GetCarsBy.Response> GetCarsByFilter(GetCarsByDto dto) {
        var request = getCarsByMapper.toRequest(dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(carService.GetWithFilter(request));
    }

    @GetMapping("/valid")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','STORAGE_ADMIN', 'ORDER_SERVICE', 'USER', 'MANAGER')")
    public ResponseEntity<CheckPossibilityToCreateAnOrder.Response> CheckPossibilityToCreateAnOrder(CheckPossibilityToCreateAnOrderDto dto) {
        var request = checkPossibilityToCreateAnOrderMapper.toRequest(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(carService.CheckPossibilityToCreateAnOrder(request));
    }

    @GetMapping("/able_testdirve")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','MANAGER', 'ORDER_SERVICE')")
    public ResponseEntity<CheckAvailableForTestDriven.Response> CheckAvailableForTestDrive(CheckAvailableForTestDriveDto dto) {
        var request = checkAvailableForTestDriveMapper.toRequest(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(carService.CheckAvailableForTestDriven(request));
    }
}
