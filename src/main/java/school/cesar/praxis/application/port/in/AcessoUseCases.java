package school.cesar.praxis.application.port.in;

import school.cesar.praxis.domain.compartilhado.ResultadoAcesso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Portas de entrada do controle de acesso aos autos: concessao/revogacao de
 * acesso ao processo, solicitacao de acesso (maquina de estados) e consulta
 * de auditoria.
 */
public interface AcessoUseCases {

    record ItemAcesso(Long id,
                      String numeroProcesso,
                      String oab,
                      String concedidoPor,
                      LocalDate concedidoEm,
                      LocalDate expiraEm,
                      LocalDate revogadoEm,
                      String motivoRevogacao,
                      boolean ativo,
                      boolean expirado,
                      Long diasParaExpirar) {
    }

    record ItemSolicitacao(Long id,
                           String numeroProcesso,
                           String oabSolicitante,
                           String justificativa,
                           LocalDate solicitadoEm,
                           String status,
                           LocalDate decididoEm,
                           String decididoPorOab,
                           String motivoNegacao) {
    }

    record ItemAuditoria(String tipoEvento,
                         String conteudoTipo,
                         Long conteudoId,
                         String numeroProcesso,
                         String oab,
                         Long usuarioId,
                         String usuarioNome,
                         LocalDateTime quando,
                         ResultadoAcesso resultado,
                         String motivo) {
    }

    interface ConcederAcesso {

        record Comando(String numeroProcesso, String oab, String concedidoPorOab, LocalDate expiraEm) {
        }

        ItemAcesso executar(Comando comando);
    }

    interface RevogarAcesso {

        record Comando(Long acessoId, String motivo, String revogadoPorOab) {
        }

        ItemAcesso executar(Comando comando);
    }

    interface ListarAcessosDoProcesso {

        List<ItemAcesso> executar(String numeroProcesso);
    }

    interface CriarSolicitacao {

        record Comando(String numeroProcesso, String oabSolicitante, String justificativa) {
        }

        ItemSolicitacao executar(Comando comando);
    }

    interface CancelarSolicitacao {

        record Comando(Long solicitacaoId, String oabSolicitante) {
        }

        ItemSolicitacao executar(Comando comando);
    }

    interface AprovarSolicitacao {

        record Comando(Long solicitacaoId, String oabChefe, LocalDate expiraEm) {
        }

        ItemSolicitacao executar(Comando comando);
    }

    interface NegarSolicitacao {

        record Comando(Long solicitacaoId, String oabChefe, String motivo) {
        }

        ItemSolicitacao executar(Comando comando);
    }

    interface ListarSolicitacoes {

        /** {@code oabFiltro} nulo traz todas (visao do chefe); informada, so as daquela OAB. */
        record Comando(String oabFiltro) {
        }

        List<ItemSolicitacao> executar(Comando comando);
    }

    interface ConsultarAuditoria {

        record Filtro(String numeroProcesso, String oab, ResultadoAcesso resultado,
                      LocalDate de, LocalDate ate) {
        }

        List<ItemAuditoria> executar(Filtro filtro);
    }
}
