package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.ProcessoEntity;

import java.util.Optional;

/** Repositorio Spring Data do processo (detalhe de infraestrutura). */
public interface ProcessoJpaRepository extends JpaRepository<ProcessoEntity, Long> {

    Optional<ProcessoEntity> findByNumeroCnj(String numeroCnj);
}
