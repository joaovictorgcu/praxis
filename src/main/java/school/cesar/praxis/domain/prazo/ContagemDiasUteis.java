package school.cesar.praxis.domain.prazo;

import java.time.LocalDate;

/** Strategy concreta: CPC art. 219 - prazos processuais em dias uteis. */
public class ContagemDiasUteis implements ContagemPrazoStrategy {

    private final CalendarioForense calendario;

    public ContagemDiasUteis(CalendarioForense calendario) {
        this.calendario = calendario;
    }

    @Override
    public LocalDate calcularVencimento(LocalDate intimacao, int quantidadeDias) {
        // Termo inicial: primeiro dia util seguinte a intimacao (art. 224 CPC).
        LocalDate cursor = calendario.proximoDiaUtil(intimacao.plusDays(1));
        int contados = 1;
        while (contados < quantidadeDias) {
            cursor = calendario.proximoDiaUtil(cursor.plusDays(1));
            contados++;
        }
        return cursor;
    }

    @Override
    public int diasRestantes(LocalDate hoje, LocalDate vencimento) {
        if (hoje.isAfter(vencimento)) {
            return 0;
        }
        int dias = 0;
        LocalDate cursor = hoje.plusDays(1);
        while (!cursor.isAfter(vencimento)) {
            if (calendario.isDiaUtil(cursor)) {
                dias++;
            }
            cursor = cursor.plusDays(1);
        }
        return dias;
    }

    @Override
    public String nome() {
        return "DIAS_UTEIS";
    }
}
