package school.cesar.praxis.domain.prazo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Strategy concreta: prazos materiais e de leis especiais, em dias corridos.
 * Vencimento em dia sem expediente forense prorroga para o proximo dia util.
 */
public class ContagemDiasCorridos implements ContagemPrazoStrategy {

    private final CalendarioForense calendario;

    public ContagemDiasCorridos(CalendarioForense calendario) {
        this.calendario = calendario;
    }

    @Override
    public LocalDate calcularVencimento(LocalDate intimacao, int quantidadeDias) {
        return calendario.proximoDiaUtil(intimacao.plusDays(quantidadeDias));
    }

    @Override
    public int diasRestantes(LocalDate hoje, LocalDate vencimento) {
        if (hoje.isAfter(vencimento)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(hoje, vencimento);
    }

    @Override
    public RegimeContagem regime() {
        return RegimeContagem.DIAS_CORRIDOS;
    }
}
