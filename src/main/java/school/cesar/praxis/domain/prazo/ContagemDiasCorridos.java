package school.cesar.praxis.domain.prazo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Strategy concreta: prazos materiais e de leis especiais, em dias corridos. */
public class ContagemDiasCorridos implements ContagemPrazoStrategy {

    private final CalendarioForense calendario;

    public ContagemDiasCorridos(CalendarioForense calendario) {
        this.calendario = calendario;
    }

    @Override
    public LocalDate calcularVencimento(LocalDate intimacao, int quantidadeDias) {
        LocalDate vencimento = intimacao.plusDays(quantidadeDias);
        // Vencimento em dia sem expediente prorroga para o proximo dia util.
        return calendario.proximoDiaUtil(vencimento);
    }

    @Override
    public int diasRestantes(LocalDate hoje, LocalDate vencimento) {
        if (hoje.isAfter(vencimento)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(hoje, vencimento);
    }

    @Override
    public String nome() {
        return "DIAS_CORRIDOS";
    }
}
