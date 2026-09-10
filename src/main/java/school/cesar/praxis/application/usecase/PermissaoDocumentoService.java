package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.out.DocumentoRepositorio;
import school.cesar.praxis.domain.documento.DocumentoGerado;

import java.util.NoSuchElementException;

@Service
public class PermissaoDocumentoService implements
        DocumentosUseCases.HabilitarOab,
        DocumentosUseCases.RevogarOab {

    private final DocumentoRepositorio documentos;

    public PermissaoDocumentoService(DocumentoRepositorio documentos) {
        this.documentos = documentos;
    }

    private DocumentoGerado buscar(Long id) {
        DocumentoGerado documento = documentos.carregar(id);
        if (documento == null) {
            throw new NoSuchElementException("documento nao encontrado: " + id);
        }
        return documento;
    }

    @Override
    @Transactional
    public DocumentoGerado executar(DocumentosUseCases.HabilitarOab.Comando comando) {
        DocumentoGerado documento = buscar(comando.documentoId());
        documento.habilitarOab(comando.oab());
        return documentos.salvar(documento);
    }

    @Override
    @Transactional
    public DocumentoGerado executar(DocumentosUseCases.RevogarOab.Comando comando) {
        DocumentoGerado documento = buscar(comando.documentoId());
        documento.revogarOab(comando.oab());
        return documentos.salvar(documento);
    }
}
