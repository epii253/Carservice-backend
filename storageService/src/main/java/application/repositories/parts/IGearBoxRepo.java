package application.repositories.parts;

import domain.entities.parts.GearBox;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository

public interface IGearBoxRepo extends ListCrudRepository<GearBox, UUID> {
}
