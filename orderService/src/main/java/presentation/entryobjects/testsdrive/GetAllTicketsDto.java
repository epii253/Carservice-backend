package presentation.entryobjects.testsdrive;

import lombok.NonNull;

import java.util.UUID;

public record GetAllTicketsDto(
        @NonNull
        UUID id
) { }
