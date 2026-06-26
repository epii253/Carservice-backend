package domain.entities;

import application.repositories.rows.TestDriveCar;
import application.repositories.rows.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity(name = "test_drive_tickets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TestDriveTicket extends BaseEntity {
    @ManyToOne
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "id"
    )
    private User user;

    @ManyToOne
    @JoinColumn(
            name = "test_drive_car_id",
            referencedColumnName = "id"
    )
    private TestDriveCar testDriveCar;

    @Column(name = "test_date")
    private Instant testDate;

    public TestDriveTicket(User user, TestDriveCar testDriveCar, Instant date) {
        this.user = user;
        this.testDriveCar = testDriveCar;
        this.testDate = date;
    }
}
