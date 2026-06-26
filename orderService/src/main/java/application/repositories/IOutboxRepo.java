package application.repositories;

import application.repositories.rows.OutboxEvent;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.UUID;

@Repository
public interface IOutboxRepo extends ListCrudRepository<OutboxEvent, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from outbox_events e where e.status in :statuses and e.availableAt <= :now order by e.createdAt")
    List<OutboxEvent> findReady(@Param("statuses") List<String> statuses, @Param("now") Instant now);
}
