package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.domain.processo.Processo;

import java.util.List;
import java.util.Optional;

/** Porta de saida: persistencia do agregado Processo. */
public interface ProcessoRepositorio {

    Processo salvar(Processo processo);

    Optional<Processo> porNumero(NumeroCnj numero);

    List<Processo> listar();
}
