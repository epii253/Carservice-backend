package application.repositories.parts;

import application.repositories.parts.stocks.RudderStock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository

public interface IRudderStockRepo extends CrudRepository<RudderStock, UUID> {

}
