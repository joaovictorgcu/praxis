package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.out.DocumentoRepositorio;
import school.cesar.praxis.domain.documento.ComandoAprovarDocumento;
import school.cesar.praxis.domain.documento.ComandoDocumento;
import school.cesar.praxis.domain.documento.ComandoRejeitarDocumento;
import school.cesar.praxis.domain.documento.DocumentoGerado;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class FluxoAprovacaoDocumentoService implements
        DocumentosUseCases.EnviarDocumentoParaRevisao,
        DocumentosUseCases.AprovarDocumento,
        DocumentosUseCases.RejeitarDocumento,
        DocumentosUseCases.DesfazerDecisaoDocumento,
        DocumentosUseCases.ProtocolarDocumento {

    private final DocumentoRepositorio documentos;
    private final Map<Long, ComandoDocumento> ultimoComando = new ConcurrentHashMap<>();

    public FluxoAprovacaoDocumentoService(DocumentoRepositorio documentos) {
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
    public DocumentoGerado executar(DocumentosUseCases.EnviarDocumentoParaRevisao.Comando comando) {
        DocumentoGerado documento = buscar(comando.documentoId());
        documento.enviarParaRevisao();
        return documentos.salvar(documento);
    }

    @Override
    @Transactional
    public DocumentoGerado executar(DocumentosUseCases.AprovarDocumento.Comando comando) {
        DocumentoGerado documento = buscar(comando.documentoId());
        ComandoDocumento cmd = new ComandoAprovarDocumento(documento, comando.oabAprovador(), comando.comentario());
        cmd.executar();
        ultimoComando.put(documento.getId(), cmd);
        return documentos.salvar(documento);
    }

    @Override
    @Transactional
    public DocumentoGerado executar(DocumentosUseCases.RejeitarDocumento.Comando comando) {
        DocumentoGerado documento = buscar(comando.documentoId());
        ComandoDocumento cmd = new ComandoRejeitarDocumento(documento, comando.oabAprovador(), comando.motivo());
        cmd.executar();
        ultimoComando.put(documento.getId(), cmd);
        return documentos.salvar(documento);
    }

    @Override
    @Transactional
    public DocumentoGerado executar(DocumentosUseCases.DesfazerDecisaoDocumento.Comando comando) {
        ComandoDocumento cmd = ultimoComando.remove(comando.documentoId());
        if (cmd == null) {
            throw new IllegalStateException("nao ha decisao para desfazer neste documento");
        }
        cmd.desfazer();
        return documentos.salvar(cmd.documento());
    }

    @Override
    @Transactional
    public DocumentoGerado executar(DocumentosUseCases.ProtocolarDocumento.Comando comando) {
        DocumentoGerado documento = buscar(comando.documentoId());
        documento.protocolar();
        return documentos.salvar(documento);
    }
}