package school.cesar.praxis.domain.compartilhado;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Abstracao de tempo do dominio. Contagem de prazo depende da data corrente, e
 * dominio que chama {@code LocalDate.now()} nao e testavel: o cenario BDD precisa
 * dizer qual dia e hoje.
 */
public interface Relogio {

    LocalDate hoje();

    /**
     * Momento para as trilhas (eventos do prazo, versoes da peca): a data vem de
     * {@link #hoje()}, para concordar com o relogio do dominio, e a hora vem do
     * sistema, so para ordenar o que aconteceu no mesmo dia.
     */
    default LocalDateTime agora() {
        return hoje().atTime(LocalTime.now());
    }
}
