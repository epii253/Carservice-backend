package presentation.controllers;

import application.contracts.dataobjects.testdrive.AddTestsDriveCar;
import application.contracts.dataobjects.testdrive.CreateTestDrive;
import application.contracts.dataobjects.testdrive.GetAllTickets;
import application.contracts.ports.ITestDriveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import presentation.entryobjects.testsdrive.AddCarForTestDrivesDto;
import presentation.entryobjects.testsdrive.GetAllTicketsDto;
import presentation.entryobjects.testsdrive.TestDriveRequestDto;
import presentation.mappers.testsdrives.IAddCarForTestDriveMapper;
import presentation.mappers.testsdrives.IGetAllTicketsDtoMapper;
import presentation.mappers.testsdrives.ITestDriveRequestDtoMapper;

@RestController
@RequestMapping("/test_drive")
public class TestDriveController {
    // Mappers
    private final ITestDriveRequestDtoMapper testDriveRequestDtoMapper;
    private final IGetAllTicketsDtoMapper getAllTicketsDtoMapper;

    private final IAddCarForTestDriveMapper addCarForTestDriveMapper;
    //

    private final ITestDriveService testDriveService;

    public TestDriveController(ITestDriveRequestDtoMapper testDriveRequestDtoMapper, IGetAllTicketsDtoMapper getAllTicketsDtoMapper, IAddCarForTestDriveMapper addCarForTestDriveMapper, ITestDriveService testDriveService) {
        this.testDriveRequestDtoMapper = testDriveRequestDtoMapper;
        this.getAllTicketsDtoMapper = getAllTicketsDtoMapper;
        this.addCarForTestDriveMapper = addCarForTestDriveMapper;
        this.testDriveService = testDriveService;
    }

    @PostMapping("/sign_up")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER', 'STORAGE_ADMIN', 'USER')")
    public ResponseEntity<CreateTestDrive.Response> TestDriveRequest(@RequestBody TestDriveRequestDto dto) {
        var request = testDriveRequestDtoMapper.toRequest(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(testDriveService.TestDriveRequest(request));
    }

    @GetMapping("/all_tickets")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER')")
    public ResponseEntity<GetAllTickets.Response> GetAllTickets(GetAllTicketsDto dto) {
        var request = getAllTicketsDtoMapper.toRequest(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(testDriveService.GetAllTestsDriveTickets(request));
    }

    @PostMapping("/add_car")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER')")
    public ResponseEntity<AddTestsDriveCar.Response> AddCarForTestDrives(@RequestBody AddCarForTestDrivesDto dto) throws Exception {
        var request = addCarForTestDriveMapper.toRequest(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(testDriveService.AddToTestDrive(request));
    }
}
