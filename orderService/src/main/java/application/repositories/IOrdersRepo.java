package application.repositories;

import domain.entities.CarOrder;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IOrdersRepo extends ListCrudRepository<CarOrder, UUID> { }
