package application.repositories.parts;

import domain.entities.parts.Transmission;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository

public interface ITransmissionRepo extends ListCrudRepository<Transmission, UUID> {
}
