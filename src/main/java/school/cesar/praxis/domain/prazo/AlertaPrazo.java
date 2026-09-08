package school.cesar.praxis.domain.prazo;

import school.cesar.praxis.domain.processo.Advogado;

import java.time.LocalDate;

/** Resultado da avaliacao do motor: um alerta pronto para ser notificado. */
public record AlertaPrazo(Long prazoId,
                          String numeroProcesso,
                          String descricaoPrazo,
                          LocalDate vencimento,
                          int diasRestantes,
                          NivelAlerta nivel,
                          boolean vencido,
                          Advogado destinatario) {
}
