package application.repositories.parts;

import application.repositories.parts.stocks.EngineStock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository

public interface IEngineStockRepo extends CrudRepository<EngineStock, UUID> {

}
