package school.cesar.praxis.domain.feriado;

import java.time.LocalDate;
import java.time.MonthDay;

/**
 * Strategy concreta: repete todo ano no mesmo dia e mes (Natal, Tiradentes).
 */
public record RecorrenciaAnualFixa(MonthDay diaEMes) implements RegraRecorrencia {

    /** Ano bissexto, para que 29/02 tambem possa ser representado. */
    private static final int ANO_DE_REFERENCIA = 2000;

    public RecorrenciaAnualFixa {
        if (diaEMes == null) {
            throw new IllegalArgumentException("dia e mes do feriado sao obrigatorios");
        }
    }

    public static RecorrenciaAnualFixa de(LocalDate referencia) {
        if (referencia == null) {
            throw new IllegalArgumentException("data de referencia e obrigatoria");
        }
        return new RecorrenciaAnualFixa(MonthDay.from(referencia));
    }

    @Override
    public boolean incideEm(LocalDate data) {
        return diaEMes.equals(MonthDay.from(data));
    }

    @Override
    public LocalDate dataDeReferencia() {
        return diaEMes.atYear(ANO_DE_REFERENCIA);
    }

    @Override
    public String rotulo() {
        return String.format("todo ano em %02d/%02d",
                diaEMes.getDayOfMonth(), diaEMes.getMonthValue());
    }
}
