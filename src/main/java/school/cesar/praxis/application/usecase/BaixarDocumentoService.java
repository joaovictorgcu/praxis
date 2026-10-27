package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.out.DocumentoRepositorio;
import school.cesar.praxis.domain.compartilhado.RegistradorDeAuditoria;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.compartilhado.Solicitante;
import school.cesar.praxis.domain.compartilhado.VerificadorDeAcessoAoProcesso;
import school.cesar.praxis.domain.documento.AcessoDocumento;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.DocumentoProxy;

/**
 * Caso de uso: ler o documento. Nunca acessa o repositorio direto - envolve o
 * repositorio no {@link DocumentoProxy} com o solicitante, de modo que a
 * checagem de segredo de justica (lista da peca OU acesso ao processo) e o
 * registro de auditoria sejam impossiveis de esquecer.
 */
@Service
public class BaixarDocumentoService implements DocumentosUseCases.BaixarDocumento {

    private final DocumentoRepositorio documentos;
    private final VerificadorDeAcessoAoProcesso verificadorAcesso;
    private final RegistradorDeAuditoria auditoria;
    private final Relogio relogio;

    public BaixarDocumentoService(DocumentoRepositorio documentos,
                                  VerificadorDeAcessoAoProcesso verificadorAcesso,
                                  RegistradorDeAuditoria auditoria,
                                  Relogio relogio) {
        this.documentos = documentos;
        this.verificadorAcesso = verificadorAcesso;
        this.auditoria = auditoria;
        this.relogio = relogio;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoGerado executar(Long documentoId, String oabSolicitante, Long usuarioId, String usuarioNome) {
        AcessoDocumento acessoProtegido = new DocumentoProxy(documentos,
                new Solicitante(oabSolicitante, usuarioId, usuarioNome),
                verificadorAcesso, auditoria, relogio.hoje());
        return acessoProtegido.carregar(documentoId);
    }
}
