package application.contracts.dataobjects.cars;

import java.util.List;
import java.util.UUID;

public record CarConfigResult(UUID modelId, List<PartEntry> parts) {}
