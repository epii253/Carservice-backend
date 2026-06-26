package application.contracts.dataobjects.carparts;

import lombok.NonNull;

import java.util.List;


public record GetAllPartsResponse(
        @NonNull
        List<PartInfo> data
) { }
