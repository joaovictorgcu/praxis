package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.SolicitacaoAcessoEntity;

import java.util.List;

/** Repositorio Spring Data da solicitacao de acesso (detalhe de infraestrutura). */
public interface SolicitacaoAcessoJpaRepository extends JpaRepository<SolicitacaoAcessoEntity, Long> {

    List<SolicitacaoAcessoEntity> findByNumeroProcessoOrderByIdDesc(String numeroProcesso);

    List<SolicitacaoAcessoEntity> findByOabSolicitanteOrderByIdDesc(String oabSolicitante);

    List<SolicitacaoAcessoEntity> findAllByOrderByIdDesc();
}
