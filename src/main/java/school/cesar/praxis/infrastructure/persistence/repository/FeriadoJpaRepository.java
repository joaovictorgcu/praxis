package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.FeriadoEntity;

/** Repositorio Spring Data do feriado (detalhe de infraestrutura). */
public interface FeriadoJpaRepository extends JpaRepository<FeriadoEntity, Long> {
}
