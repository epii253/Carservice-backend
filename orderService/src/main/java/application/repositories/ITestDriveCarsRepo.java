package application.repositories;

import application.repositories.rows.TestDriveCar;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITestDriveCarsRepo extends ListCrudRepository<TestDriveCar, UUID> {
    Optional<TestDriveCar> findByCarModel(UUID carModel);
}
