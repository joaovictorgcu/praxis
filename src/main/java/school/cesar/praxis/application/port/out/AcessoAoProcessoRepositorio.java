package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.acesso.AcessoAoProcesso;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.util.List;
import java.util.Optional;

/** Porta de saida: persistencia das concessoes de acesso ao processo. */
public interface AcessoAoProcessoRepositorio {

    AcessoAoProcesso salvar(AcessoAoProcesso acesso);

    Optional<AcessoAoProcesso> porId(Long id);

    List<AcessoAoProcesso> porProcesso(NumeroCnj numero);
}
