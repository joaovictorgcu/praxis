package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.ComarcaEntity;

public interface ComarcaJpaRepository extends JpaRepository<ComarcaEntity, Long> {}
