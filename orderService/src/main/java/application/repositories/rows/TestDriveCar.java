package application.repositories.rows;

import domain.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity(name = "test_drive_cars")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TestDriveCar extends BaseEntity {
    @Column(
            name = "test_car_model",
            nullable = false
    )
    private UUID carModel;

    public TestDriveCar(UUID carModel) {
        this.carModel = carModel;
    }
}
