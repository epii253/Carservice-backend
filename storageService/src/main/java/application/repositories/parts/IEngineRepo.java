package application.repositories.parts;

import domain.entities.parts.Engine;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface IEngineRepo extends ListCrudRepository<Engine, UUID> {
}
