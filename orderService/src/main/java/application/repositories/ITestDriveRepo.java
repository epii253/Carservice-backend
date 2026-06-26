package application.repositories;

import domain.entities.TestDriveTicket;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository

public interface ITestDriveRepo extends ListCrudRepository<TestDriveTicket, UUID> {
}
