package school.cesar.praxis.domain.distribuicao;

import school.cesar.praxis.domain.processo.Advogado;

import java.util.List;
import java.util.Optional;

/** Prioriza quem atua na area do direito do processo; entre eles, quem tem menos processos ativos. */
public class RegraPorEspecialidade extends RegraDistribuicao {

    @Override
    protected Optional<Advogado> tentar(String areaDireito, List<CandidatoDistribuicao> candidatos) {
        List<CandidatoDistribuicao> especialistas = candidatos.stream()
                .filter(c -> c.especialidade().equalsIgnoreCase(areaDireito))
                .toList();
        return comMenosProcessos(especialistas);
    }
}