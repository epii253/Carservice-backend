package application.repositories;

import domain.entities.BuildOrder;
import domain.entities.CarModel;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface IBuildOrdersRepo extends ListCrudRepository<BuildOrder, UUID>, QuerydslPredicateExecutor<BuildOrder> {
}
