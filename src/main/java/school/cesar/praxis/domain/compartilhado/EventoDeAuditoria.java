package school.cesar.praxis.domain.compartilhado;

import school.cesar.praxis.domain.processo.NumeroCnj;

import java.time.LocalDateTime;

/**
 * Um registro de auditoria de acesso aos autos: uma leitura de peca/anexo
 * (permitida ou negada) ou uma acao de gestao de acesso (concessao, revogacao,
 * solicitacao aprovada/negada). {@code conteudoTipo}/{@code conteudoId} ficam
 * nulos quando o evento nao e uma leitura.
 */
public record EventoDeAuditoria(String tipoEvento,
                                String conteudoTipo,
                                Long conteudoId,
                                NumeroCnj numeroProcesso,
                                String oab,
                                Long usuarioId,
                                String usuarioNome,
                                LocalDateTime quando,
                                ResultadoAcesso resultado,
                                String motivo) {

    /** Tentativa de leitura de peca ou anexo, como o Proxy registra. */
    public static EventoDeAuditoria leitura(String conteudoTipo, Long conteudoId, NumeroCnj numeroProcesso,
                                            Solicitante solicitante, LocalDateTime quando,
                                            ResultadoAcesso resultado, String motivo) {
        return new EventoDeAuditoria("LEITURA", conteudoTipo, conteudoId, numeroProcesso,
                solicitante.oab(), solicitante.usuarioId(), solicitante.usuarioNome(), quando, resultado, motivo);
    }

    /** Acao de gestao de acesso (concessao, revogacao, aprovacao, negacao, solicitacao). */
    public static EventoDeAuditoria gestao(String tipoEvento, NumeroCnj numeroProcesso, String oab,
                                           Long usuarioId, String usuarioNome, LocalDateTime quando,
                                           String motivo) {
        return new EventoDeAuditoria(tipoEvento, null, null, numeroProcesso, oab, usuarioId, usuarioNome,
                quando, ResultadoAcesso.PERMITIDO, motivo);
    }
}
