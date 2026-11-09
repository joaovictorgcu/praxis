package school.cesar.praxis.domain.prazo;

import java.time.LocalDate;
import java.util.function.Predicate;

/**
 * Strategy concreta: prazos processuais em dias uteis (CPC art. 219), com termo
 * inicial no primeiro dia util seguinte a intimacao (CPC art. 224).
 *
 * <p>Conta um dia quando ele e util no calendario forense <em>e</em> o prazo nao
 * estava suspenso nele. Sem suspensao, o resultado e o mesmo da contagem simples.
 */
public class ContagemDiasUteis implements ContagemPrazoStrategy {

    private final CalendarioForense calendario;

    public ContagemDiasUteis(CalendarioForense calendario) {
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
            if (calendario.isDiaUtil(cursor) && !suspenso.test(cursor)) {
                contados++;
            }
        }
        return cursor;
    }

    @Override
    public int diasRestantes(LocalDate hoje, LocalDate vencimento, Predicate<LocalDate> suspenso) {
        if (hoje.isAfter(vencimento)) {
            return 0;
        }
        int dias = 0;
        LocalDate cursor = hoje.plusDays(1);
        while (!cursor.isAfter(vencimento)) {
            if (calendario.isDiaUtil(cursor) && !suspenso.test(cursor)) {
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
