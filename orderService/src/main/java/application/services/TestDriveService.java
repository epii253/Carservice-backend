package application.services;

import application.contracts.dataobjects.testdrive.AddTestsDriveCar;
import application.contracts.dataobjects.testdrive.CreateTestDrive;
import application.contracts.dataobjects.testdrive.GetAllTickets;
import application.contracts.mappers.testdrive.ITestDriveCarMapper;
import application.contracts.mappers.testdrive.ITestDriveTicketsMapper;
import application.contracts.ports.ITestDriveService;
import application.repositories.ITestDriveCarsRepo;
import application.repositories.ITestDriveRepo;
import application.repositories.IUserRepo;
import application.repositories.rows.TestDriveCar;
import application.services.exceptions.NotFoundException;
import application.services.exceptions.UnauthorizedException;
import application.services.grpc.CarGrpcClientFacade;
import application.services.security.SecurityExtractor;
import application.services.security.TestDriveSecurity;
import domain.entities.TestDriveTicket;
import domain.valueObjects.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
public class TestDriveService implements ITestDriveService {
    // Clients
    private final CarGrpcClientFacade carGrpcClient;

    // Security
    private final TestDriveSecurity testDriveSecurity;

    private final SecurityExtractor securityExtractor;


    // Mappers
    private final ITestDriveTicketsMapper testDriveTicketsMapper;

    private final ITestDriveCarMapper testDriveCarMapper;

    // Repositories
    private final ITestDriveRepo testDriveRepo;

    private final ITestDriveCarsRepo testDriveCarsRepo;
    private final IUserRepo userRepo;


    @Value("${services.storage-service.testdrive-car-validation-path}")
    private String testdriveCarsValidationPath;

    public TestDriveService(
            CarGrpcClientFacade carGrpcClient, TestDriveSecurity testDriveSecurity, SecurityExtractor securityExtractor, ITestDriveTicketsMapper testDriveTicketsMapper, ITestDriveCarMapper testDriveCarMapper,
            ITestDriveRepo testDriveRepo,
            ITestDriveCarsRepo testDriveCarsRepo,
            IUserRepo userRepo
    ) {
        this.carGrpcClient = carGrpcClient;
        this.testDriveSecurity = testDriveSecurity;
        this.securityExtractor = securityExtractor;
        this.testDriveTicketsMapper = testDriveTicketsMapper;
        this.testDriveCarMapper = testDriveCarMapper;
        this.testDriveRepo = testDriveRepo;
        this.testDriveCarsRepo = testDriveCarsRepo;
        this.userRepo = userRepo;
    }

    public CreateTestDrive.Response TestDriveRequest(CreateTestDrive.Request request)
            throws UnauthorizedException, NotFoundException {
        var userKeycloakId = securityExtractor.getCurrentUserId();
        var user = userRepo.findByKeycloakId(UUID.fromString(userKeycloakId));

        if (user.isEmpty()) {
            throw new UnauthorizedException("no user");
        }

        var model = testDriveCarsRepo.findByCarModel(request.modelId());
        if (model.isEmpty()) {
            throw new NotFoundException("nu such  test-drive car");
        }

        var localDate = LocalDate.now().plusDays(3);
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        var ticket = testDriveRepo.save(new TestDriveTicket(user.get(), model.get(), date.toInstant()));
        return testDriveTicketsMapper.toCreateTestDriveResponse(ticket);
    }

    public GetAllTickets.Response GetAllTestsDriveTickets(GetAllTickets.Request request)
            throws UnauthorizedException {

        var userKeycloakId = securityExtractor.getCurrentUserId();
        var user = userRepo.findByKeycloakId(UUID.fromString(userKeycloakId));

        if (user.isEmpty()) {
            throw new UnauthorizedException("no user");
        }

        return testDriveTicketsMapper.toGetAllTicketsResponse(testDriveSecurity.canSeeTestDriveTicketsFilter(testDriveRepo.findAll()));
    }

    public AddTestsDriveCar.Response AddToTestDrive(AddTestsDriveCar.Request request)
            throws Exception {
        var userKeycloakId = securityExtractor.getCurrentUserId();
        var user = userRepo.findByKeycloakId(UUID.fromString(userKeycloakId));

        if (user.isEmpty()) {
            throw new UnauthorizedException("no user");
        }

        var userRole = user.get().getRole();
        if (userRole == null) {
            throw new UnauthorizedException("no user");
        }

        if (userRole == Role.USER) {
            throw new UnauthorizedException("no permission");
        }

        var grpcResponse = carGrpcClient.checkAvalForTestDriven(request.carId());
        var modelId = grpcResponse.modelId();

        var testDriveCar = testDriveCarsRepo.save(new TestDriveCar(modelId));

        return testDriveCarMapper.toAddTestsDriveCarResponse(testDriveCar);
    }
}
