package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.out.DocumentoRepositorio;
import school.cesar.praxis.application.port.out.VersaoDocumentoRepositorio;
import school.cesar.praxis.domain.compartilhado.RegistradorDeAuditoria;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.compartilhado.Solicitante;
import school.cesar.praxis.domain.compartilhado.VerificadorDeAcessoAoProcesso;
import school.cesar.praxis.domain.documento.ComparadorDeVersoes;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.DocumentoProxy;
import school.cesar.praxis.domain.documento.LinhaComparada;
import school.cesar.praxis.domain.documento.VersaoDocumento;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Leitura das versoes da peca. O texto de uma versao e a comparacao entre duas
 * passam pelo {@link DocumentoProxy}: mesma regra de sigilo da leitura da peca
 * e mesmo registro em {@code auditoria_acesso}.
 */
@Service
public class VersoesDocumentoService implements DocumentosUseCases.ConsultarVersoes {

    private final DocumentoRepositorio documentos;
    private final VersaoDocumentoRepositorio versoes;
    private final VerificadorDeAcessoAoProcesso verificadorAcesso;
    private final RegistradorDeAuditoria auditoria;
    private final Relogio relogio;

    public VersoesDocumentoService(DocumentoRepositorio documentos,
                                   VersaoDocumentoRepositorio versoes,
                                   VerificadorDeAcessoAoProcesso verificadorAcesso,
                                   RegistradorDeAuditoria auditoria,
                                   Relogio relogio) {
        this.documentos = documentos;
        this.versoes = versoes;
        this.verificadorAcesso = verificadorAcesso;
        this.auditoria = auditoria;
        this.relogio = relogio;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemVersao> listar(Long documentoId) {
        List<VersaoDocumento> todas = versoes.doDocumento(documentoId);
        int atual = todas.isEmpty() ? 0 : todas.get(todas.size() - 1).numero();
        return todas.stream()
                .map(v -> new ItemVersao(v.numero(), v.origem(), v.autorOab(), v.autorNome(), v.criadaEm(),
                        v.resumo(), v.caracteres(), v.linhas(), v.numero() == atual))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VersaoDocumento versao(Long documentoId, int numero, String oabSolicitante,
                                  Long usuarioId, String usuarioNome) {
        lerPeloProxy(documentoId, oabSolicitante, usuarioId, usuarioNome);
        return buscar(documentoId, numero);
    }

    @Override
    @Transactional(readOnly = true)
    public Comparacao comparar(Long documentoId, int de, int para, String oabSolicitante,
                               Long usuarioId, String usuarioNome) {
        if (de == para) {
            throw new IllegalArgumentException("escolha duas versões diferentes para comparar");
        }
        lerPeloProxy(documentoId, oabSolicitante, usuarioId, usuarioNome);
        VersaoDocumento antes = buscar(documentoId, de);
        VersaoDocumento depois = buscar(documentoId, para);
        List<LinhaComparada> linhas = ComparadorDeVersoes.comparar(antes.conteudo(), depois.conteudo());
        long incluidas = linhas.stream().filter(l -> l.tipo() == LinhaComparada.Tipo.INCLUIDA).count();
        long removidas = linhas.stream().filter(l -> l.tipo() == LinhaComparada.Tipo.REMOVIDA).count();
        return new Comparacao(documentoId, de, para, linhas, incluidas, removidas);
    }

    /** Lanca AcessoNegadoException (e audita a tentativa) quando a OAB nao pode ler a peca. */
    private DocumentoGerado lerPeloProxy(Long documentoId, String oab, Long usuarioId, String usuarioNome) {
        return new DocumentoProxy(documentos, new Solicitante(oab, usuarioId, usuarioNome),
                verificadorAcesso, auditoria, relogio.hoje()).carregar(documentoId);
    }

    private VersaoDocumento buscar(Long documentoId, int numero) {
        return versoes.versao(documentoId, numero)
                .orElseThrow(() -> new NoSuchElementException(
                        "versão " + numero + " não encontrada na peça " + documentoId));
    }
}
