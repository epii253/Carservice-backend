package domain.valueObjects;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Price {
    private BigDecimal value;

    public Price(float value) {
        this.value = new BigDecimal(value);
    }

    public Price(Price other) {
        value = other.value;
    }

    public Price DiffPrice(Price price) {
        return new Price(this.value.add( price.value).floatValue());
    }
}
