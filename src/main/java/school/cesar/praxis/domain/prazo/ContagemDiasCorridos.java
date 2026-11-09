package school.cesar.praxis.domain.prazo;

import java.time.LocalDate;
import java.util.function.Predicate;

/**
 * Strategy concreta: prazos materiais e de leis especiais, em dias corridos.
 * Vencimento em dia sem expediente forense prorroga para o proximo dia util.
 *
 * <p>Todo dia conta, menos os dias em que o prazo esteve suspenso.
 */
public class ContagemDiasCorridos implements ContagemPrazoStrategy {

    private final CalendarioForense calendario;

    public ContagemDiasCorridos(CalendarioForense calendario) {
        this.calendario = calendario;
    }

    @Override
    public LocalDate calcularVencimento(LocalDate intimacao, int quantidadeDias, Predicate<LocalDate> suspenso) {
        LocalDate cursor = intimacao;
        int contados = 0;
        int percorridos = 0;
        while (contados < quantidadeDias) {
            cursor = cursor.plusDays(1);
            ContagemPrazoStrategy.exigirDentroDoLimite(++percorridos);
            if (!suspenso.test(cursor)) {
                contados++;
            }
        }
        return calendario.proximoDiaUtil(cursor);
    }

    @Override
    public int diasRestantes(LocalDate hoje, LocalDate vencimento, Predicate<LocalDate> suspenso) {
        if (hoje.isAfter(vencimento)) {
            return 0;
        }
        int dias = 0;
        LocalDate cursor = hoje.plusDays(1);
        while (!cursor.isAfter(vencimento)) {
            if (!suspenso.test(cursor)) {
                dias++;
            }
            cursor = cursor.plusDays(1);
        }
        return dias;
    }

    @Override
    public RegimeContagem regime() {
        return RegimeContagem.DIAS_CORRIDOS;
    }
}
