package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.out.DocumentoRepositorio;
import school.cesar.praxis.domain.documento.AcessoDocumento;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.DocumentoProxy;

/**
 * Caso de uso: ler o documento. Nunca acessa o repositorio direto - envolve o
 * repositorio no {@link DocumentoProxy} com a OAB do solicitante, de modo que
 * a checagem de segredo de justica seja impossivel de esquecer.
 */
@Service
public class BaixarDocumentoService implements DocumentosUseCases.BaixarDocumento {

    private final DocumentoRepositorio documentos;

    public BaixarDocumentoService(DocumentoRepositorio documentos) {
        this.documentos = documentos;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoGerado executar(Long documentoId, String oabSolicitante) {
        AcessoDocumento acessoProtegido = new DocumentoProxy(documentos, oabSolicitante);
        return acessoProtegido.carregar(documentoId);
    }
}
