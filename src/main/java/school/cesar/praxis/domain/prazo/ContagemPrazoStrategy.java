package school.cesar.praxis.domain.prazo;

import java.time.LocalDate;

/**
 * <b>Strategy</b>: a lei define regimes distintos de contagem, e o agregado
 * {@link Prazo} nao deve saber qual esta em vigor.
 */
public interface ContagemPrazoStrategy {

    /** Data final do prazo, contado a partir do dia seguinte a intimacao. */
    LocalDate calcularVencimento(LocalDate intimacao, int quantidadeDias);

    /** Dias contaveis entre {@code hoje} (exclusive) e o vencimento (inclusive). */
    int diasRestantes(LocalDate hoje, LocalDate vencimento);

    RegimeContagem regime();
}
