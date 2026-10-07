package school.cesar.praxis.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import school.cesar.praxis.infrastructure.persistence.entity.ColunaKanbanEntity;

public interface ColunaKanbanJpaRepository extends JpaRepository<ColunaKanbanEntity, Long> {
    List<ColunaKanbanEntity> findAllByOrderByOrdemAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ColunaKanbanEntity c order by c.id")
    List<ColunaKanbanEntity> bloquear();
}
