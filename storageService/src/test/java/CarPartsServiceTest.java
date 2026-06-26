import application.contracts.dataobjects.carparts.*;
import application.contracts.mappers.carparts.ICarPartResponseMapper;
import application.contracts.mappers.carparts.IGetAllPartsAppMapper;
import application.repositories.parts.*;
import application.repositories.parts.stocks.*;
import application.services.CarPartsService;
import application.services.security.SecurityExtractor;
import domain.entities.parts.*;
import domain.valueObjects.EngineType;
import domain.valueObjects.ModelName;
import domain.valueObjects.PartType;
import domain.valueObjects.Price;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarPartsServiceTest {

    @Mock SecurityExtractor securityExtractor;
    @Mock IGetAllPartsAppMapper getAllPartsMapper;
    @Mock ICarPartResponseMapper carPartResponseMapper;

    @Mock IEngineRepo engineRepo;
    @Mock IEngineStockRepo engineStockRepo;
    @Mock IGearBoxRepo gearBoxRepo;
    @Mock IGearBoxStockRepo gearBoxStockRepo;
    @Mock IInteriorRepo interiorRepo;
    @Mock IInteriorStockRepo interiorStockRepo;
    @Mock IRudderRepo rudderRepo;
    @Mock IRudderStockRepo rudderStockRepo;
    @Mock ITransmissionRepo transmissionRepo;
    @Mock ITransmissionStockRepo transmissionStockRepo;
    @Mock IWheelRepo wheelRepo;
    @Mock IWheelStockRepo wheelStockRepo;

    @InjectMocks CarPartsService carPartsService;

    @Test
    void getAllParts_aggregatesAllSixRepos() {
        // Arrange
        Engine mockEngine = mock(Engine.class);
        GearBox mockGearBox = mock(GearBox.class);
        Interior mockInterior = mock(Interior.class);
        Rudder mockRudder = mock(Rudder.class);
        Transmission mockTrans = mock(Transmission.class);
        Wheel mockWheel = mock(Wheel.class);

        when(engineRepo.findAll()).thenReturn(List.of(mockEngine));
        when(gearBoxRepo.findAll()).thenReturn(List.of(mockGearBox));
        when(interiorRepo.findAll()).thenReturn(List.of(mockInterior));
        when(rudderRepo.findAll()).thenReturn(List.of(mockRudder));
        when(transmissionRepo.findAll()).thenReturn(List.of(mockTrans));
        when(wheelRepo.findAll()).thenReturn(List.of(mockWheel));

        GetAllPartsResponse expected = new GetAllPartsResponse(List.of());
        when(getAllPartsMapper.toResponse(anyList())).thenReturn(expected);

        // Act
        GetAllPartsResponse result = carPartsService.GetAllParts(new GetAllPartsRequest());

        // Assert
        assertSame(expected, result);
        verify(getAllPartsMapper).toResponse(argThat(list -> list.size() == 6));
    }

    @Test
    void addRudderPart_savesRudderAndStock_returnsPartId() {
        // Arrange
        Rudder saved = mock(Rudder.class);
        when(rudderRepo.save(any(Rudder.class))).thenReturn(saved);

        UUID partId = UUID.randomUUID();
        when(carPartResponseMapper.toResponse(saved)).thenReturn(new AddCarPartResponse(partId));

        // Act
        AddCarPartResponse result = carPartsService.AddRuderPart(
                new AddRudderRequest("TestRudder", new Price(0f), List.of())
        );

        // Assert
        assertEquals(partId, result.partId());
        verify(rudderRepo).save(any(Rudder.class));
        verify(rudderStockRepo).save(any(RudderStock.class));
    }

    @Test
    void addWheelPart_savesWheelAndStock() {
        // Arrange
        Wheel saved = mock(Wheel.class);
        when(wheelRepo.save(any(Wheel.class))).thenReturn(saved);
        when(carPartResponseMapper.toResponse(saved)).thenReturn(new AddCarPartResponse(UUID.randomUUID()));

        // Act
        carPartsService.AddWheelPart(
                new AddWheelRequest("TestWheel", new Price(0f), List.of())
        );

        // Assert
        verify(wheelRepo).save(any(Wheel.class));
        verify(wheelStockRepo).save(any(WheelStock.class));
    }

    @Test
    void addTransmissionPart_savesTransmissionAndStock() {
        // Arrange
        Transmission saved = mock(Transmission.class);
        when(transmissionRepo.save(any(Transmission.class))).thenReturn(saved);
        when(carPartResponseMapper.toResponse(saved)).thenReturn(new AddCarPartResponse(UUID.randomUUID()));

        // Act
        carPartsService.AddTransmissionPart(
                new AddTransmissionRequest("TestTransmission", new Price(0f), List.of())
        );

        // Assert
        verify(transmissionRepo).save(any(Transmission.class));
        verify(transmissionStockRepo).save(any(TransmissionStock.class));
    }

    @Test
    void addInteriorPart_savesInteriorAndStock() {
        // Arrange
        Interior saved = mock(Interior.class);
        when(interiorRepo.save(any(Interior.class))).thenReturn(saved);
        when(carPartResponseMapper.toResponse(saved)).thenReturn(new AddCarPartResponse(UUID.randomUUID()));

        // Act
        carPartsService.AddInteriorPart(
                new AddInteriorRequest("TestInterior", new Price(0f), List.of())
        );

        // Assert
        verify(interiorRepo).save(any(Interior.class));
        verify(interiorStockRepo).save(any(InteriorStock.class));
    }

    @Test
    void addEnginePart_savesEngineAndStock_returnsPartId() {
        // Arrange
        Engine saved = mock(Engine.class);
        when(engineRepo.save(any(Engine.class))).thenReturn(saved);

        UUID partId = UUID.randomUUID();
        when(carPartResponseMapper.toResponse(saved)).thenReturn(new AddCarPartResponse(partId));

        // Act
        AddCarPartResponse result = carPartsService.AddEnginePart(
                new AddEngineRequest("TestEngine", new Price(0f), List.of(), 200f, 2.0f, EngineType.Petrol)
        );

        // Assert
        assertEquals(partId, result.partId());
        verify(engineRepo).save(any(Engine.class));
        verify(engineStockRepo).save(any(EngineStock.class));
    }
}
