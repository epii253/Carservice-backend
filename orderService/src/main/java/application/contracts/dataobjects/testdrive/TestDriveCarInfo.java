package application.contracts.dataobjects.testdrive;

import java.util.UUID;

public record TestDriveCarInfo(
        UUID id,
        UUID modelId
) { }
