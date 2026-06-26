package application.contracts.ports;

import application.contracts.dataobjects.orders.*;
import application.services.exceptions.ConflictException;
import application.services.exceptions.NotFoundException;
import application.services.exceptions.UnauthorizedException;

import java.util.UUID;

public interface IOrderService {
    DoOrder.Response DoModelOrder(DoOrder.Request request)
            throws Exception;

    DoCustomOrder.Response DoCustomOrder(DoCustomOrder.Request request)
            throws Exception;

    GetAllOrders.Response GetAllOrders(GetAllOrders.Request request);

    MoveFrowardOrder.Response MoveFrowardOrder(MoveFrowardOrder.Request request);

    CanselOrder.Response CanselOrder(CanselOrder.Request request);
}
