package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.DocumentoEntity;

import java.util.List;

/** Repositorio Spring Data dos documentos gerados. */
public interface DocumentoJpaRepository extends JpaRepository<DocumentoEntity, Long> {

    List<DocumentoEntity> findByNumeroProcessoOrderByIdDesc(String numeroProcesso);
}
