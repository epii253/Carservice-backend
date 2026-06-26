package application.repositories.parts;

import application.repositories.parts.stocks.TransmissionStock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository

public interface ITransmissionStockRepo extends CrudRepository<TransmissionStock, UUID> {

}
