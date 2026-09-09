package school.cesar.praxis.domain.prazo;

import java.time.LocalDate;

/**
 * <b>Composite</b>: cada motivo de suspensao do expediente - fim de semana,
 * recesso forense, feriado cadastrado - e uma folha independente, e o
 * {@link CalendarioForense} combina todas sem saber quantas nem quais sao.
 *
 * <p>E o que permite trocar a origem dos feriados (constante em teste, cadastro
 * em banco em producao) sem tocar no calendario.
 */
public interface RegraDiaNaoUtil {

    /** A data cai em dia sem expediente forense? */
    boolean suspende(LocalDate data);
}
