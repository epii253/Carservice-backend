import application.contracts.dataobjects.testdrive.*;
import application.contracts.mappers.testdrive.ITestDriveCarMapper;
import application.contracts.mappers.testdrive.ITestDriveTicketsMapper;
import application.repositories.ITestDriveCarsRepo;
import application.repositories.ITestDriveRepo;
import application.repositories.IUserRepo;
import application.repositories.rows.TestDriveCar;
import application.repositories.rows.User;
import application.services.TestDriveService;
import application.services.exceptions.NotFoundException;
import application.services.exceptions.UnauthorizedException;
import application.services.grpc.CarGrpcClientFacade;
import application.services.security.SecurityExtractor;
import application.services.security.TestDriveSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import application.contracts.dataobjects.cars.TestDriveAvailability;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestDriveServiceTest {

    @Mock CarGrpcClientFacade carGrpcClient;
    @Mock TestDriveSecurity testDriveSecurity;
    @Mock SecurityExtractor securityExtractor;
    @Mock ITestDriveTicketsMapper testDriveTicketsMapper;
    @Mock ITestDriveCarMapper testDriveCarMapper;
    @Mock ITestDriveRepo testDriveRepo;
    @Mock ITestDriveCarsRepo testDriveCarsRepo;
    @Mock IUserRepo userRepo;

    @InjectMocks TestDriveService testDriveService;

    private static final UUID USER_ID    = UUID.randomUUID();
    private static final UUID MANAGER_ID = UUID.randomUUID();

    private User regularUser;
    private User manager;

    @BeforeEach
    void setUp() {
        regularUser = new User(USER_ID, "user1", "USER");
        manager     = new User(MANAGER_ID, "manager1", "MANAGER");
    }

    @Test
    void testDriveRequest_noUser_throwsUnauthorized() {
        // Arrange
        when(securityExtractor.getCurrentUserId()).thenReturn(USER_ID.toString());
        when(userRepo.findByKeycloakId(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
            testDriveService.TestDriveRequest(
                new CreateTestDrive.Request(USER_ID, UUID.randomUUID()))
        );
    }

    @Test
    void testDriveRequest_noTestDriveCar_throwsNotFoundException() {
        // Arrange
        UUID modelId = UUID.randomUUID();
        when(securityExtractor.getCurrentUserId()).thenReturn(USER_ID.toString());
        when(userRepo.findByKeycloakId(USER_ID)).thenReturn(Optional.of(regularUser));
        when(testDriveCarsRepo.findByCarModel(modelId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
            testDriveService.TestDriveRequest(new CreateTestDrive.Request(USER_ID, modelId))
        );
    }

    @Test
    void addToTestDrive_asRegularUser_throwsUnauthorized() {
        // Arrange
        when(securityExtractor.getCurrentUserId()).thenReturn(USER_ID.toString());
        when(userRepo.findByKeycloakId(USER_ID)).thenReturn(Optional.of(regularUser));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
            testDriveService.AddToTestDrive(
                new AddTestsDriveCar.Request(UUID.randomUUID(), USER_ID))
        );
    }

    @Test
    void addToTestDrive_asManager_callsGrpcAndSaves() throws Exception {
        // Arrange
        UUID carId   = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();

        when(securityExtractor.getCurrentUserId()).thenReturn(MANAGER_ID.toString());
        when(userRepo.findByKeycloakId(MANAGER_ID)).thenReturn(Optional.of(manager));

        TestDriveAvailability grpcResp = new TestDriveAvailability(modelId, true);
        when(carGrpcClient.checkAvalForTestDriven(carId)).thenReturn(grpcResp);

        TestDriveCar savedCar = new TestDriveCar(modelId);
        when(testDriveCarsRepo.save(any(TestDriveCar.class))).thenReturn(savedCar);

        AddTestsDriveCar.Response expected =
                new AddTestsDriveCar.Response(new TestDriveCarInfo(UUID.randomUUID(), modelId));
        when(testDriveCarMapper.toAddTestsDriveCarResponse(savedCar)).thenReturn(expected);

        // Act
        AddTestsDriveCar.Response result =
                testDriveService.AddToTestDrive(new AddTestsDriveCar.Request(carId, MANAGER_ID));

        // Assert
        assertNotNull(result);
        verify(carGrpcClient).checkAvalForTestDriven(carId);
        verify(testDriveCarsRepo).save(any(TestDriveCar.class));
    }
}
