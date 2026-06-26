package application.repositories.parts;

import application.repositories.parts.stocks.InteriorStock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository

public interface IInteriorStockRepo extends CrudRepository<InteriorStock, UUID> {

}
