package presentation.entryobjects.orders;

import lombok.NonNull;

import java.util.UUID;

public record CanselOrderDto (
    @NonNull
    UUID orderId

) { }
