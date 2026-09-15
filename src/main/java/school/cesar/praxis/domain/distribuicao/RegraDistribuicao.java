package school.cesar.praxis.domain.distribuicao;

import school.cesar.praxis.domain.processo.Advogado;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Handler da cadeia de responsabilidade que decide qual advogado assume um
 * processo novo. Cada regra tenta resolver por um criterio; se nao conseguir,
 * repassa para a proxima regra da cadeia.
 */
public abstract class RegraDistribuicao {

    private RegraDistribuicao proxima;

    public RegraDistribuicao proximaRegra(RegraDistribuicao proxima) {
        this.proxima = proxima;
        return proxima;
    }

    public final Advogado distribuir(String areaDireito, List<CandidatoDistribuicao> candidatos) {
        Optional<Advogado> resolvido = tentar(areaDireito, candidatos);
        if (resolvido.isPresent()) {
            return resolvido.get();
        }
        if (proxima != null) {
            return proxima.distribuir(areaDireito, candidatos);
        }
        throw new IllegalStateException("nenhum advogado disponivel para distribuicao");
    }

    protected abstract Optional<Advogado> tentar(String areaDireito, List<CandidatoDistribuicao> candidatos);

    protected Optional<Advogado> comMenosProcessos(List<CandidatoDistribuicao> candidatos) {
        return candidatos.stream()
                .min(Comparator.comparingInt(CandidatoDistribuicao::processosAtivos))
                .map(CandidatoDistribuicao::advogado);
    }
}