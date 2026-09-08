package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.documento.AcessoDocumento;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.util.List;

/**
 * Porta de saida: persistencia de documentos. Estende {@link AcessoDocumento}
 * para poder ser o objeto real por tras do {@code DocumentoProxy}.
 */
public interface DocumentoRepositorio extends AcessoDocumento {

    DocumentoGerado salvar(DocumentoGerado documento);

    List<DocumentoGerado> porProcesso(NumeroCnj numero);

    List<DocumentoGerado> listar();
}
