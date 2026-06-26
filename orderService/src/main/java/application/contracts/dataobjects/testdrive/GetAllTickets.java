package application.contracts.dataobjects.testdrive;

import java.util.List;
import java.util.UUID;

public class GetAllTickets {
    private GetAllTickets() {}
    public record Request(UUID id) {};

    public record Response(List<TestDriveTicketInfo> ticketsInfo) {};
}
