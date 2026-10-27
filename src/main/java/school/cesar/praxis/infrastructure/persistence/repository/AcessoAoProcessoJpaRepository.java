package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.AcessoAoProcessoEntity;

import java.util.List;

/** Repositorio Spring Data do acesso ao processo (detalhe de infraestrutura). */
public interface AcessoAoProcessoJpaRepository extends JpaRepository<AcessoAoProcessoEntity, Long> {

    List<AcessoAoProcessoEntity> findByNumeroProcessoOrderByIdDesc(String numeroProcesso);
}
