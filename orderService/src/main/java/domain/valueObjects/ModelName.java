package domain.valueObjects;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@Getter
public class ModelName {
    String name;

    public ModelName(String name) {
        this.name = String.valueOf(name);
    }

    public ModelName(ModelName other) {
        name = String.valueOf(other.getName());
    }
}
