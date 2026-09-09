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

        /**
         * @param codigoModelo modelo cadastrado a usar; nulo cai no gerador
         *                     compilado do {@code tipo}.
         */
        record Comando(String numeroProcesso,
                       TipoDocumento tipo,
                       Map<String, String> campos,
                       String oabSolicitante,
                       String codigoModelo) {

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
}
