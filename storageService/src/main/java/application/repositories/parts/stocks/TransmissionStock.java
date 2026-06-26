package application.repositories.parts.stocks;

import domain.entities.BaseEntity;
import domain.entities.parts.Transmission;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "transmission_stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransmissionStock extends BaseEntity {
    @ManyToOne
    @JoinColumn(
            name = "transmission_id",
            referencedColumnName = "id"
    )
    private Transmission transmission;

    public TransmissionStock(Transmission transmission) {
        this.transmission = transmission;
    }
}