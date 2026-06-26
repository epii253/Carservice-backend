package application.services;

import application.contracts.dataobjects.orders.*;
import application.contracts.mappers.orders.IOrderMapper;
import application.contracts.ports.IOrderService;
import application.outbox.payloads.CarOrderEventPayload;
import application.outbox.payloads.EventPayload;
import application.repositories.IOrdersRepo;
import application.repositories.IOutboxRepo;
import application.repositories.IUserRepo;
import application.repositories.rows.OutboxEvent;
import application.repositories.rows.QUser;
import application.services.exceptions.ConflictException;
import application.services.exceptions.NotFoundException;
import application.services.exceptions.UnauthorizedException;
import application.services.grpc.CarGrpcClientFacade;
import application.services.security.OrderSecurity;
import application.services.security.SecurityExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.entities.CarOrder;
import domain.entities.OrderState;
import domain.entities.Pair;
import domain.valueObjects.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.stream.StreamSupport;
@Service
@Transactional
public class OrderService implements IOrderService {
    // Mappers
    private final ObjectMapper objectMapper;

    // Clients

    private final CarGrpcClientFacade carGrpcClient;
    // Security
    private final SecurityExtractor securityExtractor;

    private final OrderSecurity orderSecurity;

    // Mappers
    private final IOrderMapper orderMapper;

    // Repositories
    private final IOutboxRepo outboxRepo;
    private final IOrdersRepo ordersRepo;
    private final IUserRepo userRepo;
    private final Random rand;

    @Value("${services.storage-service.car-validation-path}")
    private String carsValidationPath;

    public OrderService(
            ObjectMapper objectMapper, CarGrpcClientFacade carGrpcClient, SecurityExtractor securityExtractor, OrderSecurity orderSecurity, IOrderMapper orderMapper, IOutboxRepo outboxRepo, IOrdersRepo ordersRepo,
            IUserRepo userRepo
    ) {
        this.objectMapper = objectMapper;
        this.carGrpcClient = carGrpcClient;
        this.securityExtractor = securityExtractor;
        this.orderSecurity = orderSecurity;
        this.orderMapper = orderMapper;
        this.outboxRepo = outboxRepo;
        this.ordersRepo = ordersRepo;
        this.userRepo = userRepo;
        this.rand = new Random();
    }

    @Override
    public DoOrder.Response DoModelOrder(DoOrder.Request request)
            throws Exception {

        var allManagers = StreamSupport.stream(userRepo.findAll(QUser.user.role.eq(Role.MANAGER)).spliterator(), false).toList();

        var userKeycloakId = securityExtractor.getCurrentUserId();
        var user = userRepo.findByKeycloakId(UUID.fromString(userKeycloakId));

        if (user.isEmpty()) {
            throw new UnauthorizedException("no user");
        }

        if (allManagers.isEmpty()) {
            throw new UnauthorizedException("no managers");
        }

        var result = carGrpcClient.checkCarConfig(
                new ModelName(request.modelName()),
                new Color(request.color()),
                null,
                null,
                null,
                null,
                null
        );

        var modelId = result.modelId();
        var parts = result.parts().stream()
                .map(p -> new Pair<>(p.partName(), p.partId()))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        var order = new CarOrder(
                user.get(),
                allManagers.get(rand.nextInt(allManagers.size())),
                OrderType.Premade,
                parts,
                modelId
        );

        order = ordersRepo.save(order);

        return orderMapper.toOrderResponse(order);
    }

    @Override
    public DoCustomOrder.Response DoCustomOrder(DoCustomOrder.Request request)
            throws Exception {

        var allManagers = StreamSupport.stream(userRepo.findAll(QUser.user.role.eq(Role.MANAGER)).spliterator(), false).toList();

        var userKeycloakId = securityExtractor.getCurrentUserId();
        var user = userRepo.findByKeycloakId(UUID.fromString(userKeycloakId));

        if (user.isEmpty()) {
            throw new UnauthorizedException("no user");
        }

        if (allManagers.isEmpty()) {
            throw new UnauthorizedException("no managers");
        }

        var result = carGrpcClient.checkCarConfig(
                request.modelName(),
                request.color(),
                request.rudderId(),
                request.wheelId(),
                request.transmissionId(),
                request.interiorId(),
                request.engineId()
        );

        var modelId = result.modelId();
        var parts = result.parts().stream()
                .map(p -> new Pair<>(p.partName(), p.partId()))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        var order = new CarOrder(
                user.get(),
                allManagers.get(rand.nextInt(allManagers.size())),
                OrderType.Custom,
                parts,
                modelId
        );

        order = ordersRepo.save(order);

        return orderMapper.toCustomOrderResponse(order);
    }

    @Override
    public GetAllOrders.Response GetAllOrders(GetAllOrders.Request request) {
        var userKeycloakId = securityExtractor.getCurrentUserId();
        var user = userRepo.findByKeycloakId(UUID.fromString(userKeycloakId));

        if (user.isEmpty()) {
            throw new UnauthorizedException("no user");
        }

        return orderMapper.toAllOrdersResponse(orderSecurity.canSeeOrderFilter(ordersRepo.findAll()));
    }

    @Override
    public MoveFrowardOrder.Response MoveFrowardOrder(MoveFrowardOrder.Request request) {
        var order = ordersRepo.findById(request.orderId());

        if (order.isEmpty()) {
            throw new NotFoundException("no order");
        }

        if (!order.get().getState().Next(order.get())) {
            throw new ConflictException("cannot do next");
        }

        var state = order.get().getState();

        if (state == OrderState.Paid) {
            enqueueCarOrderEvent(
                    CarOrderEventPayload.builder()
                            .orderId(order.get().getId())
                            .orderType(order.get().getOrderType().toString())
                            .modelId(order.get().getModelId())
                            .parts(order.get().getParts())
                            .build()
                    , Instant.now(), "OrderSentForApproval");
        }

        var movedOrder = ordersRepo.save(order.get());

        return new MoveFrowardOrder.Response(movedOrder.getState().toString());
    }

    @Override
    public CanselOrder.Response CanselOrder(CanselOrder.Request request) {
        var order = ordersRepo.findById(request.orderId());

        if (order.isEmpty()) {
            throw new NotFoundException("no order");
        }

        if (!order.get().getState().Cancel(order.get())) {
            throw new ConflictException("cannot do cansel");
        }

        ordersRepo.save(order.get());

        return new CanselOrder.Response();
    }

    private void enqueueCarOrderEvent(EventPayload payload, Instant now, String eventType) {
        var event = new OutboxEvent(
                eventType,
                payload.toPayload(objectMapper),
                "NEW",
                0,
                now
        );

        outboxRepo.save(event);
    }
}
