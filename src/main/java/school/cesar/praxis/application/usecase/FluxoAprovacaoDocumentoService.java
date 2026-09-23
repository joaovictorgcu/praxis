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

@Service
public class FluxoAprovacaoDocumentoService implements
        DocumentosUseCases.EnviarDocumentoParaRevisao,
        DocumentosUseCases.AprovarDocumento,
        DocumentosUseCases.RejeitarDocumento,
        DocumentosUseCases.DesfazerDecisaoDocumento,
        DocumentosUseCases.ProtocolarDocumento {

    private final DocumentoRepositorio documentos;

    public FluxoAprovacaoDocumentoService(DocumentoRepositorio documentos) {
        this.documentos = documentos;
    }

    private DocumentoGerado buscar(Long id) {
        DocumentoGerado documento = documentos.carregar(id);
        if (documento == null) {
            throw new NoSuchElementException("documento não encontrado: " + id);
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
        return documentos.salvar(documento);
    }

    @Override
    @Transactional
    public DocumentoGerado executar(DocumentosUseCases.RejeitarDocumento.Comando comando) {
        DocumentoGerado documento = buscar(comando.documentoId());
        ComandoDocumento cmd = new ComandoRejeitarDocumento(documento, comando.oabAprovador(), comando.motivo());
        cmd.executar();
        return documentos.salvar(documento);
    }

    @Override
    @Transactional
    public DocumentoGerado executar(DocumentosUseCases.DesfazerDecisaoDocumento.Comando comando) {
        // O desfazer opera sobre o agregado recem-carregado, e nao sobre um snapshot
        // guardado em memoria: sobrevive a reinicio e nao descarta alteracoes feitas
        // entre a decisao e o desfazer (ex.: OAB habilitada depois da aprovacao).
        DocumentoGerado documento = buscar(comando.documentoId());
        documento.desfazerUltimaDecisao();
        return documentos.salvar(documento);
    }

    @Override
    @Transactional
    public DocumentoGerado executar(DocumentosUseCases.ProtocolarDocumento.Comando comando) {
        DocumentoGerado documento = buscar(comando.documentoId());
        documento.protocolar();
        return documentos.salvar(documento);
    }
}