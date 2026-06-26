package application.repositories.parts.stocks;

import domain.entities.BaseEntity;
import domain.entities.parts.Interior;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "interior_stocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class InteriorStock extends BaseEntity {
    @ManyToOne
    @JoinColumn(
            name = "interior_id",
            referencedColumnName = "id"
    )
    private Interior interior;

    public InteriorStock(Interior interior) {
        this.interior = interior;
    }
}
