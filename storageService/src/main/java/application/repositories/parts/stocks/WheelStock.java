package application.repositories.parts.stocks;

import domain.entities.BaseEntity;
import domain.entities.parts.Wheel;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "wheel_stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WheelStock extends BaseEntity {
    @ManyToOne
    @JoinColumn(
            name = "wheel_id",
            referencedColumnName = "id"
    )
    private Wheel wheel;

    public WheelStock(Wheel wheel) {
        this.wheel = wheel;
    }
}