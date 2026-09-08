package school.cesar.praxis.domain.prazo;

import java.time.LocalDate;

/**
 * Strategy: a lei define regimes distintos de contagem (CPC art. 219 x prazos
 * em dias corridos de leis especiais). O agregado Prazo nao sabe qual regime usa.
 */
public interface ContagemPrazoStrategy {

    /** Data final do prazo, contado a partir do dia seguinte a intimacao. */
    LocalDate calcularVencimento(LocalDate intimacao, int quantidadeDias);

    /** Quantos dias contaveis faltam entre {@code hoje} e o vencimento. */
    int diasRestantes(LocalDate hoje, LocalDate vencimento);

    String nome();
}
