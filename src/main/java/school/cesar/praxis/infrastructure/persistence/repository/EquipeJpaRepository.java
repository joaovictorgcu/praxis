package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.EquipeEntity;

public interface EquipeJpaRepository extends JpaRepository<EquipeEntity, Long> {
}
