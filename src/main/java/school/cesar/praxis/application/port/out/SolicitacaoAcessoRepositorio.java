package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.acesso.SolicitacaoAcesso;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.util.List;
import java.util.Optional;

/** Porta de saida: persistencia das solicitacoes de acesso. */
public interface SolicitacaoAcessoRepositorio {

    SolicitacaoAcesso salvar(SolicitacaoAcesso solicitacao);

    Optional<SolicitacaoAcesso> porId(Long id);

    List<SolicitacaoAcesso> porProcesso(NumeroCnj numero);

    List<SolicitacaoAcesso> listar();

    List<SolicitacaoAcesso> porOabSolicitante(String oab);
}
