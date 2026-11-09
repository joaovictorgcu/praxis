package school.cesar.praxis.domain.prazo;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Uma execucao da varredura, gravada em {@code varredura_execucao}: prova de que
 * o motor rodou (inclusive quando nao havia nada a alertar), com a data de
 * referencia usada, quantos prazos avaliou e quantos alertas emitiu.
 */
public record ExecucaoVarredura(Long id,
                                OrigemVarredura origem,
                                LocalDate dataReferencia,
                                LocalDateTime iniciadaEm,
                                LocalDateTime concluidaEm,
                                int prazosAvaliados,
                                int alertasEmitidos,
                                String solicitante) {

    public ExecucaoVarredura {
        if (origem == null) {
            throw new IllegalArgumentException("varredura exige a origem");
        }
        if (dataReferencia == null || iniciadaEm == null || concluidaEm == null) {
            throw new IllegalArgumentException("varredura exige data de referência, início e fim");
        }
        if (concluidaEm.isBefore(iniciadaEm)) {
            throw new IllegalArgumentException("varredura não pode terminar antes de começar");
        }
        if (prazosAvaliados < 0 || alertasEmitidos < 0) {
            throw new IllegalArgumentException("contagens da varredura não podem ser negativas");
        }
    }

    public long duracaoEmMilissegundos() {
        return Duration.between(iniciadaEm, concluidaEm).toMillis();
    }

    /** Rodou com uma data diferente do dia em que foi executada ("Simular Data"). */
    public boolean dataSimulada() {
        return !dataReferencia.equals(iniciadaEm.toLocalDate());
    }
}
