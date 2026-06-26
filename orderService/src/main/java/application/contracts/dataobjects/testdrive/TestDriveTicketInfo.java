package application.contracts.dataobjects.testdrive;

import java.time.Instant;
import java.util.UUID;

public record TestDriveTicketInfo(
    UUID id,
    UUID userId,
    UUID testsDriveModelId,
    UUID modelId,
    Instant testDate
) { }
