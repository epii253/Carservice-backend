package application.repositories.parts;

import domain.entities.parts.Wheel;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository

public interface IWheelRepo extends ListCrudRepository<Wheel, UUID> {
}
