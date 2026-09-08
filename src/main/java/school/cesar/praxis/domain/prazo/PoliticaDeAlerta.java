package school.cesar.praxis.domain.prazo;

import java.util.Optional;

/**
 * Politica do escritorio para alerta de prazos: em quantos dias restantes cada
 * nivel dispara, e se prazos nao fatais tambem entram na regua.
 *
 * <p>O nivel escolhido e sempre o mais severo aplicavel: faltando 1 dia, o alerta
 * e CRITICO, nao ATENCAO.
 */
public class PoliticaDeAlerta {

    private final boolean alertarNaoFatais;

    public PoliticaDeAlerta(boolean alertarNaoFatais) {
        this.alertarNaoFatais = alertarNaoFatais;
    }

    /** Politica padrao: so prazos fatais geram alerta. */
    public static PoliticaDeAlerta padrao() {
        return new PoliticaDeAlerta(false);
    }

    public static PoliticaDeAlerta inclusiva() {
        return new PoliticaDeAlerta(true);
    }

    public Optional<NivelAlerta> nivelPara(int diasRestantes, boolean fatal) {
        if (!fatal && !alertarNaoFatais) {
            return Optional.empty();
        }
        if (diasRestantes <= NivelAlerta.VENCE_HOJE.diasRestantes()) {
            return Optional.of(NivelAlerta.VENCE_HOJE);
        }
        if (diasRestantes <= NivelAlerta.CRITICO.diasRestantes()) {
            return Optional.of(NivelAlerta.CRITICO);
        }
        if (diasRestantes <= NivelAlerta.URGENTE.diasRestantes()) {
            return Optional.of(NivelAlerta.URGENTE);
        }
        if (diasRestantes <= NivelAlerta.ATENCAO.diasRestantes()) {
            return Optional.of(NivelAlerta.ATENCAO);
        }
        return Optional.empty();
    }
}
