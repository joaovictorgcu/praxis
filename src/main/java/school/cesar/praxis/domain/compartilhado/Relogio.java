package school.cesar.praxis.domain.compartilhado;

import java.time.LocalDate;

/**
 * Abstracao de tempo do dominio. Contagem de prazo depende da data corrente, e
 * dominio que chama {@code LocalDate.now()} nao e testavel: o cenario BDD precisa
 * dizer qual dia e hoje.
 */
public interface Relogio {

    LocalDate hoje();
}
