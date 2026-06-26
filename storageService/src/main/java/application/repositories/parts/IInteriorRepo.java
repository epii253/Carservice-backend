package application.repositories.parts;

import domain.entities.parts.Interior;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository

public interface IInteriorRepo extends ListCrudRepository<Interior, UUID> {
}
