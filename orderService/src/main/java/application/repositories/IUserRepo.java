package application.repositories;

import application.repositories.rows.User;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository

public interface IUserRepo extends ListCrudRepository<User, UUID>, QuerydslPredicateExecutor<User> {
    Optional<User> findByKeycloakId(UUID keycloakId);
}
