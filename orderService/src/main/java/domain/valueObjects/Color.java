package domain.valueObjects;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Color {
    private String name;

    public Color(String name) {
        this.name = String.valueOf(name);
    }
    public Color(Color other) {
        name = other.name;
    }
}
