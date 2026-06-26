package application.contracts.dataobjects.cars;

import java.util.UUID;

public record TestDriveAvailability(UUID modelId, boolean isAvailable) {}
