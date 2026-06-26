package domain.valueObjects;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CarCase {
    String name;

    public CarCase(String name) {
        this.name = String.valueOf(name);
    }
    public CarCase(CarCase other) {
        name = String.valueOf(other.name);
    }
}
