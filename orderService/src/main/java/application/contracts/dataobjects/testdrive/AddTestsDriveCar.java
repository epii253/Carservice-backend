package application.contracts.dataobjects.testdrive;

import java.util.UUID;

public class AddTestsDriveCar {
    private AddTestsDriveCar() {}
    public record Request(
        UUID carId,
        UUID userId
    ) {}

    public record Response(TestDriveCarInfo testDriveCarInfo) {}
}
