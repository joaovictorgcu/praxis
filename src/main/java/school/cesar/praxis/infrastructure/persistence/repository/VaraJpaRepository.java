package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.VaraEntity;

public interface VaraJpaRepository extends JpaRepository<VaraEntity, Long> {}
