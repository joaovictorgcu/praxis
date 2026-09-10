package school.cesar.praxis.domain.distribuicao;

import school.cesar.praxis.domain.processo.Advogado;

import java.util.List;
import java.util.Optional;

/** Quando ninguem da especialidade esta livre, escolhe entre os disponiveis com menos processos ativos. */
public class RegraPorDisponibilidade extends RegraDistribuicao {

    @Override
    protected Optional<Advogado> tentar(String areaDireito, List<CandidatoDistribuicao> candidatos) {
        List<CandidatoDistribuicao> disponiveis = candidatos.stream()
                .filter(CandidatoDistribuicao::disponivel)
                .toList();
        return comMenosProcessos(disponiveis);
    }
}