package school.cesar.praxis.domain.prazo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/** Servico de dominio: sabe quais dias nao contam para prazo processual. */
public class CalendarioForense {

    private final Set<LocalDate> feriados;

    public CalendarioForense(Set<LocalDate> feriados) {
        this.feriados = Set.copyOf(feriados);
    }

    public boolean isDiaUtil(LocalDate data) {
        if (data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return false;
        }
        if (feriados.contains(data)) {
            return false;
        }
        // Art. 220 CPC: suspensao de prazos entre 20/12 e 20/01.
        int mes = data.getMonthValue();
        int dia = data.getDayOfMonth();
        boolean recesso = (mes == 12 && dia >= 20) || (mes == 1 && dia <= 20);
        return !recesso;
    }

    public LocalDate proximoDiaUtil(LocalDate data) {
        LocalDate cursor = data;
        while (!isDiaUtil(cursor)) {
            cursor = cursor.plusDays(1);
        }
        return cursor;
    }
}
