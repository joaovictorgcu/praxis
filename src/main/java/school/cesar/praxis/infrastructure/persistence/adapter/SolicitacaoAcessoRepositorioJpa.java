package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.SolicitacaoAcessoRepositorio;
import school.cesar.praxis.domain.acesso.SolicitacaoAcesso;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.SolicitacaoAcessoJpaRepository;

import java.util.List;
import java.util.Optional;

/** Adaptador de saida das solicitacoes de acesso. */
@Repository
public class SolicitacaoAcessoRepositorioJpa implements SolicitacaoAcessoRepositorio {

    private final SolicitacaoAcessoJpaRepository jpa;

    public SolicitacaoAcessoRepositorioJpa(SolicitacaoAcessoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public SolicitacaoAcesso salvar(SolicitacaoAcesso solicitacao) {
        return PersistenciaMapper.paraDominio(jpa.save(PersistenciaMapper.paraEntidade(solicitacao)));
    }

    @Override
    public Optional<SolicitacaoAcesso> porId(Long id) {
        return jpa.findById(id).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public List<SolicitacaoAcesso> porProcesso(NumeroCnj numero) {
        return jpa.findByNumeroProcessoOrderByIdDesc(numero.valor()).stream()
                .map(PersistenciaMapper::paraDominio)
                .toList();
    }

    @Override
    public List<SolicitacaoAcesso> listar() {
        return jpa.findAllByOrderByIdDesc().stream().map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    public List<SolicitacaoAcesso> porOabSolicitante(String oab) {
        return jpa.findByOabSolicitanteOrderByIdDesc(oab).stream().map(PersistenciaMapper::paraDominio).toList();
    }
}
