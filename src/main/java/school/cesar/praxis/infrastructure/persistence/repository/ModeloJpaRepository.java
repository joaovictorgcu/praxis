package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.ModeloEntity;

import java.util.Optional;

/** Repositorio Spring Data do modelo de peca (detalhe de infraestrutura). */
public interface ModeloJpaRepository extends JpaRepository<ModeloEntity, Long> {

    Optional<ModeloEntity> findByCodigo(String codigo);
}
