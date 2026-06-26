package presentation.controllers;

import application.contracts.dataobjects.orders.DoCustomOrder;
import application.contracts.dataobjects.orders.DoOrder;
import application.contracts.dataobjects.orders.GetAllOrders;
import application.contracts.dataobjects.orders.MoveFrowardOrder;
import application.contracts.ports.IOrderService;
import application.services.grpc.CarGrpcClientFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import presentation.entryobjects.orders.*;
import presentation.mappers.orders.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order")
public class OrdersController {
    // Mappers
    private final IMoveForwardOrderMapper moveForwardOrderMapper;
    private final IDoModelOrderMapper doModelOrderMapper;
    private final IDoCustomModelOrderMapper doCustomModelOrderMapper;
    private final IGetAllOrdersMapper getAllOrdersMapper;
    private final ICanselOrderMapper canselOrderMapper;
    private final ICarInfoMapper carInfoMapper;

    // Services
    private final IOrderService orderService;
    private final CarGrpcClientFacade carGrpcClient;

    public OrdersController(IMoveForwardOrderMapper moveForwardOrderMapper, IDoModelOrderMapper doModelOrderMapper, IDoCustomModelOrderMapper doCustomModelOrderMapper, IGetAllOrdersMapper getAllOrdersMapper, ICanselOrderMapper canselOrderMapper, ICarInfoMapper carInfoMapper, IOrderService orderService, CarGrpcClientFacade carGrpcClient) {
        this.moveForwardOrderMapper = moveForwardOrderMapper;
        this.doModelOrderMapper = doModelOrderMapper;
        this.doCustomModelOrderMapper = doCustomModelOrderMapper;
        this.getAllOrdersMapper = getAllOrdersMapper;
        this.canselOrderMapper = canselOrderMapper;
        this.carInfoMapper = carInfoMapper;
        this.orderService = orderService;
        this.carGrpcClient = carGrpcClient;
    }

    @PostMapping("/model")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER', 'STORAGE_ADMIN', 'USER')")
    public ResponseEntity<DoOrder.Response> DoModelOrder(@RequestBody DoModelOrderDto dto) throws Exception {
        var request = doModelOrderMapper.toRequest(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.DoModelOrder(request));
    }

    @PutMapping("/moveOn")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER')")
    public ResponseEntity<MoveFrowardOrder.Response> MoveForwardOrder(@RequestBody MoveForwardOrderDto dto) {
        var request = moveForwardOrderMapper.toRequest(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderService.MoveFrowardOrder(request));
    }

    @PatchMapping("/cansel")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER', 'STORAGE_ADMIN', 'USER')")
    public ResponseEntity<?> CanselOrder(@RequestBody CanselOrderDto dto) {
        var request = canselOrderMapper.toRequest(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderService.CanselOrder(request));
    }

    @PostMapping("/custom")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER', 'STORAGE_ADMIN', 'USER')")
    public ResponseEntity<DoCustomOrder.Response> DoCustomModelOrder(@RequestBody DoCustomModelOrderDto dto) throws Exception {
        var request = doCustomModelOrderMapper.toRequest(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.DoCustomOrder(request));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER', 'STORAGE_ADMIN', 'USER')")
    public ResponseEntity<GetAllOrders.Response> GetAllOrders(GetAllOrdersDto dto) {
        var request = getAllOrdersMapper.toRequest(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderService.GetAllOrders(request));
    }

    @GetMapping("/avaliables")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<CarInfoResponse>> GetAllAvailable() throws Throwable {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(carInfoMapper.toResponseList(carGrpcClient.getAllCars()));
    }

    @GetMapping("/avaliables/{carId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<CarInfoResponse>> GetAvailableById(@PathVariable UUID carId) throws Throwable {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(carInfoMapper.toResponseList(List.of(carGrpcClient.getCarById(carId))));
    }
}
