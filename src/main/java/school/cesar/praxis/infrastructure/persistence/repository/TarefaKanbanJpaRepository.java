package school.cesar.praxis.infrastructure.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.TarefaKanbanEntity;

public interface TarefaKanbanJpaRepository extends JpaRepository<TarefaKanbanEntity, Long> {
    List<TarefaKanbanEntity> findAllByOrderByOrdemAscIdAsc();
}
