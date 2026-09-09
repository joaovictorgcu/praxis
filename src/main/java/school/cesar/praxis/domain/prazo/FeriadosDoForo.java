package school.cesar.praxis.domain.prazo;

import school.cesar.praxis.domain.feriado.Feriado;
import school.cesar.praxis.domain.feriado.FonteDeFeriados;
import school.cesar.praxis.domain.feriado.Jurisdicao;

import java.time.LocalDate;

/**
 * Folha do Composite que consulta os feriados cadastrados, filtrados pela
 * abrangencia do foro.
 *
 * <p>A leitura acontece a cada pergunta, de proposito: feriado cadastrado agora
 * ja vale na proxima contagem. Prazo <b>ja lancado</b> nao se move, porque o
 * {@link Prazo} congela o vencimento no momento da abertura.
 */
public class FeriadosDoForo implements RegraDiaNaoUtil {

    private final FonteDeFeriados fonte;
    private final Jurisdicao foro;

    public FeriadosDoForo(FonteDeFeriados fonte, Jurisdicao foro) {
        if (fonte == null) {
            throw new IllegalArgumentException("fonte de feriados e obrigatoria");
        }
        if (foro == null) {
            throw new IllegalArgumentException("foro e obrigatorio");
        }
        this.fonte = fonte;
        this.foro = foro;
    }

    @Override
    public boolean suspende(LocalDate data) {
        for (Feriado feriado : fonte.vigentes()) {
            if (feriado.suspendeExpediente(data, foro)) {
                return true;
            }
        }
        return false;
    }
}
