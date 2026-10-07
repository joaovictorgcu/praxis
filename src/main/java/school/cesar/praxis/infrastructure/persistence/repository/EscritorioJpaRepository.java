package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.EscritorioEntity;

import java.util.Optional;

/** Repositorio Spring Data do escritorio (detalhe de infraestrutura). */
public interface EscritorioJpaRepository extends JpaRepository<EscritorioEntity, Long> {

    Optional<EscritorioEntity> findByEmail(String email);
}
