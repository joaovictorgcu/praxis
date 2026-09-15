package school.cesar.praxis.domain.distribuicao;

import school.cesar.praxis.domain.processo.Advogado;

import java.util.List;
import java.util.Optional;

/** Fallback: distribui para quem tiver menos processos ativos, sem outro criterio. */
public class RegraPadrao extends RegraDistribuicao {

    @Override
    protected Optional<Advogado> tentar(String areaDireito, List<CandidatoDistribuicao> candidatos) {
        return comMenosProcessos(candidatos);
    }
}