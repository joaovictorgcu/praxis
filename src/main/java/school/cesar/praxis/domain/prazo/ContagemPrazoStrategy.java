package school.cesar.praxis.domain.prazo;

import java.time.LocalDate;
import java.util.function.Predicate;

/**
 * <b>Strategy</b>: a lei define regimes distintos de contagem, e o agregado
 * {@link Prazo} nao deve saber qual esta em vigor.
 *
 * <p>As duas operacoes recebem o predicado {@code suspenso}: dia em que o prazo
 * esteve suspenso nao conta (CPC art. 221), entao recontar o prazo desde a
 * intimacao, pulando os dias suspensos, devolve exatamente o saldo que faltava
 * quando a suspensao comecou. Sem suspensao, vale {@link #SEM_SUSPENSAO}.
 */
public interface ContagemPrazoStrategy {

    /** Nenhum dia suspenso: a contagem original, sem ajuste. */
    Predicate<LocalDate> SEM_SUSPENSAO = dia -> false;

    /**
     * Teto de seguranca das contagens: um laco que passasse disso estaria
     * contando contra uma suspensao sem fim, o que o agregado nunca deve pedir.
     */
    int LIMITE_DE_DIAS_PERCORRIDOS = 3_650;

    /** Data final do prazo, contado a partir do dia seguinte a intimacao. */
    default LocalDate calcularVencimento(LocalDate intimacao, int quantidadeDias) {
        return calcularVencimento(intimacao, quantidadeDias, SEM_SUSPENSAO);
    }

    /** Data final do prazo, sem contar os dias em que esteve suspenso. */
    LocalDate calcularVencimento(LocalDate intimacao, int quantidadeDias, Predicate<LocalDate> suspenso);

    /** Dias contaveis entre {@code hoje} (exclusive) e o vencimento (inclusive). */
    default int diasRestantes(LocalDate hoje, LocalDate vencimento) {
        return diasRestantes(hoje, vencimento, SEM_SUSPENSAO);
    }

    /** Dias contaveis e nao suspensos entre {@code hoje} (exclusive) e o vencimento (inclusive). */
    int diasRestantes(LocalDate hoje, LocalDate vencimento, Predicate<LocalDate> suspenso);

    RegimeContagem regime();

    /** Interrompe a contagem que nao converge, em vez de travar a requisicao. */
    static void exigirDentroDoLimite(int percorridos) {
        if (percorridos > LIMITE_DE_DIAS_PERCORRIDOS) {
            throw new IllegalStateException(
                    "contagem do prazo não converge: mais de " + LIMITE_DE_DIAS_PERCORRIDOS + " dias percorridos");
        }
    }
}
