package application.contracts.dataobjects.orders;

import domain.entities.Pair;

import java.util.List;
import java.util.UUID;

public record OrderInfo(
    UUID id,

    UUID userId,
    UUID managerId,
    UUID modelId,

    List<Pair<String, UUID>> parts,

    String orderType,
    String orderState
) { }
