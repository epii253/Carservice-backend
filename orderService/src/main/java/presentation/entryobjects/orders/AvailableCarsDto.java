package presentation.entryobjects.orders;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AvailableCarsDto {
    private UUID carId;
    public AvailableCarsDto() {}
}
