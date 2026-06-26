package domain.valueObjects;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Embeddable
@NoArgsConstructor
public class UserId {
    private UUID id;

    public UserId (UUID id) {
        this.id = id;
    }

    public UserId(UserId other) {
        this.id = other.getId();
    }
}
