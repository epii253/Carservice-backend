package application.contracts.dataobjects.testdrive;

import java.util.UUID;

public class CreateTestDrive {
    private CreateTestDrive() {}
    public record Request(UUID userId, UUID modelId) {};

    public record Response(TestDriveTicketInfo ticketInfo) {};
}