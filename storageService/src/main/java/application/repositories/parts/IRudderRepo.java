package application.repositories.parts;

import domain.entities.parts.Rudder;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository

public interface IRudderRepo extends ListCrudRepository<Rudder, UUID> {
}
