package school.cesar.praxis.domain.prazo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro imutavel da trilha do prazo: quem fez o que, quando, por que e o que
 * aconteceu com o vencimento. A trilha so cresce - nenhum evento e alterado ou
 * apagado depois de gravado.
 */
public record EventoPrazo(Long id,
                          Long prazoId,
                          TipoEventoPrazo tipo,
                          LocalDateTime ocorridoEm,
                          LocalDate vencimentoAnterior,
                          LocalDate vencimentoNovo,
                          String detalhe,
                          String autorOab,
                          String autorNome) {

    public EventoPrazo {
        if (prazoId == null) {
            throw new IllegalArgumentException("evento exige o prazo");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("evento exige o tipo");
        }
        if (ocorridoEm == null) {
            throw new IllegalArgumentException("evento exige o momento");
        }
    }

    public static EventoPrazo novo(Long prazoId, TipoEventoPrazo tipo, LocalDateTime quando,
                                   LocalDate vencimentoAnterior, LocalDate vencimentoNovo,
                                   String detalhe, String autorOab, String autorNome) {
        return new EventoPrazo(null, prazoId, tipo, quando, vencimentoAnterior, vencimentoNovo,
                detalhe, autorOab, autorNome);
    }

    /** Evento que mudou a data de vencimento (retomada, dilacao). */
    public boolean moveuVencimento() {
        return vencimentoAnterior != null && vencimentoNovo != null
                && !vencimentoAnterior.equals(vencimentoNovo);
    }
}
