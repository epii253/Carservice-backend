package application.repositories.parts.stocks;

import domain.entities.BaseEntity;
import domain.entities.parts.Rudder;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "rudder_stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RudderStock extends BaseEntity {
    @ManyToOne
    @JoinColumn(
            name = "rudder_id",
            referencedColumnName = "id"
    )
    private Rudder rudder;

    public RudderStock(Rudder rudder) {
        this.rudder = rudder;
    }
}
