package domain.valueObjects;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandName {
    private String name;

    public BrandName(String name) {
        this.name = name;
    }
    public BrandName(BrandName other) {
        name = String.valueOf(other.name);
    }
}
