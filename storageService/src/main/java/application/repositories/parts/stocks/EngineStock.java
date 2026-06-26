package application.repositories.parts.stocks;

import domain.entities.BaseEntity;
import domain.entities.parts.Engine;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "engine_stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EngineStock extends BaseEntity {
    @ManyToOne
    @JoinColumn(
            name = "engine_id",
            referencedColumnName = "id"
    )
    private Engine engine;

    public EngineStock(Engine engine) {
        this.engine = engine;
    }
}
