package school.cesar.praxis.application.port.in;

import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.TipoDocumento;

import java.util.List;
import java.util.Map;

/**
 * Portas de entrada da funcionalidade <b>Geracao de documentos por template</b>
 * (peticao inicial, contestacao, procuracao).
 */
public interface DocumentosUseCases {

    interface GerarDocumento {

        record Comando(String numeroProcesso,
                       TipoDocumento tipo,
                       Map<String, String> campos,
                       String oabSolicitante) {
        }

        DocumentoGerado executar(Comando comando);
    }

    /** Leitura passa pelo Proxy de segredo de justica. */
    interface BaixarDocumento {

        DocumentoGerado executar(Long documentoId, String oabSolicitante);
    }

    interface ListarDocumentos {

        record ItemDocumento(Long id,
                             String numeroProcesso,
                             TipoDocumento tipo,
                             String geradoEm,
                             boolean segredoJustica) {
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
}
