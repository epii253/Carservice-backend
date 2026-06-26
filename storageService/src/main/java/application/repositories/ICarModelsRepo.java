package application.repositories;


import domain.entities.CarModel;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ICarModelsRepo extends ListCrudRepository<CarModel, UUID>, JpaSpecificationExecutor<CarModel> {
}
