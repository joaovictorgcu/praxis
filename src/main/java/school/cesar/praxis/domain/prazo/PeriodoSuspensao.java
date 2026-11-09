package school.cesar.praxis.domain.prazo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Value Object: intervalo em que o prazo nao correu. {@code fim} e o ultimo dia
 * suspenso (inclusive); nulo enquanto a suspensao continua em aberto.
 */
public record PeriodoSuspensao(LocalDate inicio, LocalDate fim, String motivo) {

    public PeriodoSuspensao {
        if (inicio == null) {
            throw new IllegalArgumentException("a suspensão exige data de início");
        }
        if (fim != null && fim.isBefore(inicio)) {
            throw new IllegalArgumentException("a suspensão não pode terminar antes de começar");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("a suspensão exige motivo");
        }
        motivo = motivo.trim();
    }

    public static PeriodoSuspensao aberta(LocalDate inicio, String motivo) {
        return new PeriodoSuspensao(inicio, null, motivo);
    }

    public boolean emAberto() {
        return fim == null;
    }

    public boolean contem(LocalDate dia) {
        return !dia.isBefore(inicio) && (fim == null || !dia.isAfter(fim));
    }

    /** Fecha a suspensao: {@code ultimoDia} e o ultimo dia em que o prazo ficou parado. */
    public PeriodoSuspensao encerradaEm(LocalDate ultimoDia) {
        return new PeriodoSuspensao(inicio, ultimoDia, motivo);
    }

    /** Dias corridos parados; para a suspensao aberta, conta ate {@code hoje}. */
    public long diasParado(LocalDate hoje) {
        LocalDate ate = fim == null ? hoje : fim;
        return ate.isBefore(inicio) ? 0 : ChronoUnit.DAYS.between(inicio, ate) + 1;
    }
}
