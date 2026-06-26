package application.repositories.parts.stocks;

import domain.entities.BaseEntity;
import domain.entities.parts.GearBox;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "gear_box_stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GearBoxStock extends BaseEntity {
    @ManyToOne
    @JoinColumn(
            name = "gear_box_id",
            referencedColumnName = "id"
    )
    private GearBox gearBox;

    public GearBoxStock(GearBox gearBox) {
        this.gearBox = gearBox;
    }
}

