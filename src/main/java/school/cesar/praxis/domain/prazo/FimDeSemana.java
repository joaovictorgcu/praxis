package school.cesar.praxis.domain.prazo;

import java.time.DayOfWeek;
import java.time.LocalDate;

/** Folha do Composite: nao corre prazo em sabado nem domingo. */
public class FimDeSemana implements RegraDiaNaoUtil {

    @Override
    public boolean suspende(LocalDate data) {
        DayOfWeek dia = data.getDayOfWeek();
        return dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;
    }
}
