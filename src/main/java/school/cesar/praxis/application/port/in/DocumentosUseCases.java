package school.cesar.praxis.application.port.in;

import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.LinhaComparada;
import school.cesar.praxis.domain.documento.OrigemVersao;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.domain.documento.VersaoDocumento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Portas de entrada da funcionalidade <b>Geracao de documentos por template</b>
 * (peticao inicial, contestacao, procuracao).
 */
public interface DocumentosUseCases {

    interface GerarDocumento {

        /**
         * @param codigoModelo    modelo cadastrado a usar; nulo cai no gerador
         *                        compilado do {@code tipo}.
         * @param nomeSolicitante quem pediu a peca, para a versao 1 do texto.
         */
        record Comando(String numeroProcesso,
                       TipoDocumento tipo,
                       Map<String, String> campos,
                       String oabSolicitante,
                       String codigoModelo,
                       String nomeSolicitante) {

            /** Geracao sem o nome de quem pediu (carga de exemplo, testes): a versao 1 fica so com a OAB. */
            public Comando(String numeroProcesso,
                           TipoDocumento tipo,
                           Map<String, String> campos,
                           String oabSolicitante,
                           String codigoModelo) {
                this(numeroProcesso, tipo, campos, oabSolicitante, codigoModelo, null);
            }

            /** Geracao sem modelo: usa a peca compilada. */
            public Comando(String numeroProcesso,
                           TipoDocumento tipo,
                           Map<String, String> campos,
                           String oabSolicitante) {
                this(numeroProcesso, tipo, campos, oabSolicitante, null);
            }
        }

        DocumentoGerado executar(Comando comando);
    }

    /** Leitura passa pelo Proxy de segredo de justica. */
    interface BaixarDocumento {

        /** Chamada direta (BDD, scripts): sem usuario logado, so a auditoria registra "desconhecido". */
        default DocumentoGerado executar(Long documentoId, String oabSolicitante) {
            return executar(documentoId, oabSolicitante, null, null);
        }

        /** Chamada do painel/API: carrega quem, alem da OAB, para a auditoria. */
        DocumentoGerado executar(Long documentoId, String oabSolicitante, Long usuarioId, String usuarioNome);
    }

    interface ListarDocumentos {

        /** {@code status} e o nome do estado do fluxo de aprovacao (RASCUNHO, EM_REVISAO...). */
        record ItemDocumento(Long id,
                             String numeroProcesso,
                             TipoDocumento tipo,
                             String geradoEm,
                             String geradoPorOab,
                             boolean segredoJustica,
                             String status,
                             int versao) {
        }

        List<ItemDocumento> executar(String numeroProcesso);
    }

        interface EnviarDocumentoParaRevisao {
        record Comando(Long documentoId) {}
        DocumentoGerado executar(Comando comando);
    }

    interface AprovarDocumento {
        record Comando(Long documentoId, String oabAprovador, String comentario) {}
        DocumentoGerado executar(Comando comando);
    }

    interface RejeitarDocumento {
        record Comando(Long documentoId, String oabAprovador, String motivo) {}
        DocumentoGerado executar(Comando comando);
    }

    interface DesfazerDecisaoDocumento {
        record Comando(Long documentoId) {}
        DocumentoGerado executar(Comando comando);
    }

    interface ProtocolarDocumento {
        record Comando(Long documentoId) {}
        DocumentoGerado executar(Comando comando);
    }

    interface HabilitarOab {
        record Comando(Long documentoId, String oab) {}
        DocumentoGerado executar(Comando comando);
    }

    interface RevogarOab {
        record Comando(Long documentoId, String oab) {}
        DocumentoGerado executar(Comando comando);
    }

    /**
     * Edicao manual do texto: vira a versao seguinte em {@code documento_versao}.
     * So rascunho ou peca rejeitada; o autor precisa poder ler a peca.
     */
    interface EditarDocumento {
        record Comando(Long documentoId, String conteudo, String resumo, String autorOab, String autorNome) {}
        DocumentoGerado executar(Comando comando);
    }

    /**
     * Refaz o texto com o mesmo gerador da geracao (modelo cadastrado ou peca
     * compilada) e campos novos, mesclados sobre os da geracao.
     */
    interface RegenerarDocumento {
        record Comando(Long documentoId, Map<String, String> campos, String autorOab, String autorNome) {}
        DocumentoGerado executar(Comando comando);
    }

    /** O texto de uma versao anterior volta a ser o atual, como versao nova (o historico nao se apaga). */
    interface RestaurarVersao {
        record Comando(Long documentoId, int numero, String autorOab, String autorNome) {}
        DocumentoGerado executar(Comando comando);
    }

    /** Exclusao definitiva de rascunho nunca revisado, com registro na auditoria. */
    interface ExcluirDocumento {
        record Comando(Long documentoId, String autorOab, boolean chefe, Long usuarioId, String usuarioNome) {}
        void executar(Comando comando);
    }

    /**
     * Leitura das versoes. A lista traz so metadados (o mesmo nivel da lista de
     * pecas); o texto de uma versao e a comparacao passam pelo Proxy de sigilo
     * e ficam na auditoria como leitura da peca.
     */
    interface ConsultarVersoes {
        record ItemVersao(int numero,
                          OrigemVersao origem,
                          String autorOab,
                          String autorNome,
                          LocalDateTime criadaEm,
                          String resumo,
                          int caracteres,
                          int linhas,
                          boolean atual) {
        }

        record Comparacao(Long documentoId,
                          int de,
                          int para,
                          List<LinhaComparada> linhas,
                          long incluidas,
                          long removidas) {
        }

        List<ItemVersao> listar(Long documentoId);

        VersaoDocumento versao(Long documentoId, int numero, String oabSolicitante, Long usuarioId, String usuarioNome);

        Comparacao comparar(Long documentoId, int de, int para, String oabSolicitante, Long usuarioId, String usuarioNome);
    }
}
