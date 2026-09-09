package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.ArquivoEntity;

import java.util.List;

/** Repositorio Spring Data do anexo (detalhe de infraestrutura). */
public interface ArquivoJpaRepository extends JpaRepository<ArquivoEntity, Long> {

    List<ArquivoEntity> findByNumeroProcessoOrderByIdAsc(String numeroProcesso);
}
