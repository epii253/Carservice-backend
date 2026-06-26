package application.repositories.parts;

import application.repositories.parts.stocks.GearBoxStock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository

public interface IGearBoxStockRepo extends CrudRepository<GearBoxStock, UUID> {

}
