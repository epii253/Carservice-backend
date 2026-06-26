import application.contracts.dataobjects.orders.*;
import application.contracts.mappers.orders.IOrderMapper;
import application.repositories.IOrdersRepo;
import application.repositories.IOutboxRepo;
import application.repositories.IUserRepo;
import application.repositories.rows.User;
import application.services.OrderService;
import application.services.exceptions.ConflictException;
import application.services.exceptions.NotFoundException;
import application.services.exceptions.UnauthorizedException;
import application.services.grpc.CarGrpcClientFacade;
import application.services.security.OrderSecurity;
import application.services.security.SecurityExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Predicate;
import domain.entities.CarOrder;
import domain.entities.OrderState;
import domain.valueObjects.Color;
import domain.valueObjects.ModelName;
import domain.valueObjects.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import application.contracts.dataobjects.cars.CarConfigResult;
import application.contracts.dataobjects.cars.PartEntry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock ObjectMapper objectMapper;
    @Mock CarGrpcClientFacade carGrpcClient;
    @Mock SecurityExtractor securityExtractor;
    @Mock OrderSecurity orderSecurity;
    @Mock IOrderMapper orderMapper;
    @Mock IOutboxRepo outboxRepo;
    @Mock IOrdersRepo ordersRepo;
    @Mock IUserRepo userRepo;

    @InjectMocks OrderService orderService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID MANAGER_ID = UUID.randomUUID();

    private User customer;
    private User manager;

    @BeforeEach
    void setUp() {
        customer = new User(USER_ID, "customer", "USER");
        manager  = new User(MANAGER_ID, "manager", "MANAGER");
    }

    //  DoModelOrder 

    @Test
    void doModelOrder_noUser_throwsUnauthorized() {
        // Arrange
        when(userRepo.findAll(any(Predicate.class))).thenReturn(List.of(manager));
        when(securityExtractor.getCurrentUserId()).thenReturn(USER_ID.toString());
        when(userRepo.findByKeycloakId(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
            orderService.DoModelOrder(new DoOrder.Request("TestModel", "Red"))
        );
    }

    @Test
    void doModelOrder_noManagers_throwsUnauthorized() {
        // Arrange
        when(userRepo.findAll(any(Predicate.class))).thenReturn(List.of());
        when(securityExtractor.getCurrentUserId()).thenReturn(USER_ID.toString());
        when(userRepo.findByKeycloakId(USER_ID)).thenReturn(Optional.of(customer));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
            orderService.DoModelOrder(new DoOrder.Request("TestModel", "Red"))
        );
    }

    @Test
    void doModelOrder_success_savesOrderAndReturnsResponse() throws Exception {
        // Arrange
        UUID modelId = UUID.randomUUID();
        CarConfigResult grpcResp = new CarConfigResult(
                modelId,
                List.of(new PartEntry("engine", UUID.randomUUID()))
        );

        when(userRepo.findAll(any(Predicate.class))).thenReturn(List.of(manager));
        when(securityExtractor.getCurrentUserId()).thenReturn(USER_ID.toString());
        when(userRepo.findByKeycloakId(USER_ID)).thenReturn(Optional.of(customer));
        when(carGrpcClient.checkCarConfig(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(grpcResp);

        CarOrder savedOrder = mock(CarOrder.class);
        when(ordersRepo.save(any(CarOrder.class))).thenReturn(savedOrder);

        DoOrder.Response expected = new DoOrder.Response(UUID.randomUUID(), MANAGER_ID);
        when(orderMapper.toOrderResponse(savedOrder)).thenReturn(expected);

        // Act
        DoOrder.Response result = orderService.DoModelOrder(new DoOrder.Request("TestModel", "Red"));

        // Assert
        assertNotNull(result);
        assertEquals(expected, result);
        verify(ordersRepo).save(any(CarOrder.class));
    }

    //  DoCustomOrder 

    @Test
    void doCustomOrder_noUser_throwsUnauthorized() {
        // Arrange
        when(userRepo.findAll(any(Predicate.class))).thenReturn(List.of(manager));
        when(securityExtractor.getCurrentUserId()).thenReturn(USER_ID.toString());
        when(userRepo.findByKeycloakId(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
            orderService.DoCustomOrder(new DoCustomOrder.Request(
                new ModelName("TestModel"), new Color("Red"),
                null, null, null, null, null
            ))
        );
    }

    @Test
    void doCustomOrder_success_returnsResponse() throws Exception {
        // Arrange
        UUID modelId = UUID.randomUUID();
        CarConfigResult grpcResp = new CarConfigResult(modelId, List.of());

        when(userRepo.findAll(any(Predicate.class))).thenReturn(List.of(manager));
        when(securityExtractor.getCurrentUserId()).thenReturn(USER_ID.toString());
        when(userRepo.findByKeycloakId(USER_ID)).thenReturn(Optional.of(customer));
        when(carGrpcClient.checkCarConfig(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(grpcResp);

        CarOrder savedOrder = mock(CarOrder.class);
        when(ordersRepo.save(any(CarOrder.class))).thenReturn(savedOrder);

        DoCustomOrder.Response expected = new DoCustomOrder.Response(UUID.randomUUID(), MANAGER_ID);
        when(orderMapper.toCustomOrderResponse(savedOrder)).thenReturn(expected);

        // Act
        DoCustomOrder.Response result = orderService.DoCustomOrder(new DoCustomOrder.Request(
            new ModelName("TestModel"), new Color("Red"), null, null, null, null, null
        ));

        // Assert
        assertNotNull(result);
        assertEquals(expected, result);
        verify(carGrpcClient).checkCarConfig(any(), any(), any(), any(), any(), any(), any());
    }

    //  MoveFrowardOrder 

    @Test
    void moveFrowardOrder_orderNotFound_throwsNotFoundException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(ordersRepo.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
            orderService.MoveFrowardOrder(new MoveFrowardOrder.Request(orderId))
        );
    }

    @Test
    void moveFrowardOrder_onCanceledOrder_throwsConflictException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        CarOrder order = new CarOrder(customer, manager, OrderType.Premade, List.of(), UUID.randomUUID());
        order.SetState(OrderState.Canceled);

        when(ordersRepo.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(ConflictException.class, () ->
            orderService.MoveFrowardOrder(new MoveFrowardOrder.Request(orderId))
        );
    }

    //  CanselOrder

    @Test
    void canselOrder_orderNotFound_throwsNotFoundException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(ordersRepo.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
            orderService.CanselOrder(new CanselOrder.Request(orderId))
        );
    }
}
