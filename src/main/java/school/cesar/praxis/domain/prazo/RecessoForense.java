package school.cesar.praxis.domain.prazo;

import java.time.LocalDate;

/**
 * Folha do Composite: suspensao de prazos entre 20/12 e 20/01 (CPC art. 220).
 *
 * <p>E regra de lei, nao cadastro: por isso continua no codigo em vez de virar
 * um {@code Feriado} que o usuario pudesse apagar por engano.
 */
public class RecessoForense implements RegraDiaNaoUtil {

    @Override
    public boolean suspende(LocalDate data) {
        int mes = data.getMonthValue();
        int dia = data.getDayOfMonth();
        return (mes == 12 && dia >= 20) || (mes == 1 && dia <= 20);
    }
}
