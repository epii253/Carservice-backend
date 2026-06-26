package application.repositories;

import application.repositories.rows.RequiredPartStock;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IRequiredPartRepo extends ListCrudRepository<RequiredPartStock, UUID> {
}
