package school.cesar.praxis.domain.prazo;

import java.time.LocalDate;

/**
 * Strategy concreta: prazos processuais em dias uteis (CPC art. 219), com termo
 * inicial no primeiro dia util seguinte a intimacao (CPC art. 224).
 */
public class ContagemDiasUteis implements ContagemPrazoStrategy {

    private final CalendarioForense calendario;

    public ContagemDiasUteis(CalendarioForense calendario) {
        this.calendario = calendario;
    }

    @Override
    public LocalDate calcularVencimento(LocalDate intimacao, int quantidadeDias) {
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
    public RegimeContagem regime() {
        return RegimeContagem.DIAS_UTEIS;
    }
}
