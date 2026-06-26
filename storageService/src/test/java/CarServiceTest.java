import application.contracts.dataobjects.cars.*;
import application.contracts.mappers.cars.ICarMapper;
import application.outbox.publishers.KafkaRegularPublisher;
import application.repositories.IBuildOrdersRepo;
import application.repositories.ICarModelsRepo;
import application.repositories.IRequiredPartRepo;
import application.repositories.parts.*;
import application.services.CarService;
import application.services.KeycloakService;
import application.services.exceptions.NotFoundException;
import application.services.security.SecurityExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.entities.CarModel;
import domain.entities.GearBoxType;
import domain.valueObjects.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock SecurityExtractor securityExtractor;
    @Mock KeycloakService keycloakService;
    @Mock ICarMapper carMapper;
    @Mock ObjectMapper objectMapper;
    @Mock IBuildOrdersRepo buildOrdersRepo;
    @Mock ICarModelsRepo carModelsRepo;
    @Mock IRudderRepo rudderRepo;
    @Mock IWheelRepo wheelRepo;
    @Mock ITransmissionRepo transmissionRepo;
    @Mock IInteriorRepo interiorRepo;
    @Mock IEngineRepo engineRepo;
    @Mock IRequiredPartRepo requiredPartRepo;
    @Mock KafkaRegularPublisher kafkaRegularPublisher;

    @InjectMocks CarService carService;

    // --- CheckAvailableForTestDriven ---

    @Test
    void checkAvailableForTestDriven_existingModel_returnsTrue() {
        // Arrange
        UUID modelId = UUID.randomUUID();
        when(carModelsRepo.findById(modelId)).thenReturn(Optional.of(mock(CarModel.class)));

        // Act
        CheckAvailableForTestDriven.Response result =
                carService.CheckAvailableForTestDriven(new CheckAvailableForTestDriven.Request(modelId));

        // Assert
        assertEquals(modelId, result.modelId());
        assertTrue(result.isAvailable());
    }

    @Test
    void checkAvailableForTestDriven_unknownModel_returnsFalse() {
        // Arrange
        UUID modelId = UUID.randomUUID();
        when(carModelsRepo.findById(modelId)).thenReturn(Optional.empty());

        // Act
        CheckAvailableForTestDriven.Response result =
                carService.CheckAvailableForTestDriven(new CheckAvailableForTestDriven.Request(modelId));

        // Assert
        assertEquals(modelId, result.modelId());
        assertFalse(result.isAvailable());
    }

    // --- GetAllCars ---

    @Test
    void getAllCars_callsMapperWithAllModels() {
        // Arrange
        List<CarModel> models = List.of(mock(CarModel.class), mock(CarModel.class));
        when(carModelsRepo.findAll()).thenReturn(models);

        GetAllCars.Response expected = new GetAllCars.Response(List.of());
        when(carMapper.toGetAllCarsResponse(models)).thenReturn(expected);

        // Act
        GetAllCars.Response result = carService.GetAllCars(GetAllCars.Request.builder().build());

        // Assert
        assertSame(expected, result);
        verify(carMapper).toGetAllCarsResponse(models);
    }

    // --- GetCarById ---

    @Test
    void getCarById_found_returnsCarInfo() {
        // Arrange
        UUID carId = UUID.randomUUID();
        CarModel model = mock(CarModel.class);
        when(carModelsRepo.findById(carId)).thenReturn(Optional.of(model));

        GetCarById.Response expected = mock(GetCarById.Response.class);
        when(carMapper.toGetCarByIdResponse(model)).thenReturn(expected);

        // Act
        GetCarById.Response result = carService.GetCarById(new GetCarById.Request(carId));

        // Assert
        assertSame(expected, result);
    }

    @Test
    void getCarById_notFound_throwsNotFoundException() {
        // Arrange
        var carId = UUID.randomUUID();
        when(carModelsRepo.findById(carId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                carService.GetCarById(new GetCarById.Request(carId))
        );
    }

    // --- AddNewCarModel ---

    @Test
    void addNewCarModel_partNotFound_throwsNotFoundException() {
        // Arrange
        var rudderId = UUID.randomUUID();
        var wheelId = UUID.randomUUID();
        var transmissionId = UUID.randomUUID();
        var interiorId = UUID.randomUUID();
        var engineId = UUID.randomUUID();

        when(rudderRepo.existsById(rudderId)).thenReturn(false);

        NewCar.Request request = new NewCar.Request(
                List.of(PartType.Rudder),
                new ModelName("TestModel"),
                new CarCase("Sedan"),
                new BrandName("BMW"),
                new Color("Black"),
                new Price(50000f),
                Wheeldrive.All,
                GearBoxType.Automatic,
                rudderId,
                wheelId,
                transmissionId,
                interiorId,
                engineId
        );

        // Act & Assert
        assertThrows(NotFoundException.class, () -> carService.AddNewCarModel(request));
    }
}
