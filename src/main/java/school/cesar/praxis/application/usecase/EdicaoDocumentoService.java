package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.out.DocumentoRepositorio;
import school.cesar.praxis.application.port.out.VersaoDocumentoRepositorio;
import school.cesar.praxis.domain.compartilhado.EventoDeAuditoria;
import school.cesar.praxis.domain.compartilhado.PoliticaDeLeituraDosAutos;
import school.cesar.praxis.domain.compartilhado.ProxyDeAcesso;
import school.cesar.praxis.domain.compartilhado.RegistradorDeAuditoria;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.compartilhado.VerificadorDeAcessoAoProcesso;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.OrigemVersao;
import school.cesar.praxis.domain.documento.VersaoDocumento;

import java.util.NoSuchElementException;

/**
 * Casos de uso que mudam o texto ou a existencia da peca: editar, restaurar
 * versao e excluir rascunho.
 *
 * <p>Cada mudanca de texto grava, na mesma transacao, a peca com a versao
 * incrementada e a linha nova em {@code documento_versao}. O agregado decide
 * se cabe (o estado do fluxo de aprovacao e a regra de exclusao); aqui fica o
 * que depende de banco: conferir o acesso aos autos e apagar em cascata.
 */
@Service
public class EdicaoDocumentoService implements
        DocumentosUseCases.EditarDocumento,
        DocumentosUseCases.RestaurarVersao,
        DocumentosUseCases.ExcluirDocumento {

    /** Tipo do evento na auditoria de acesso quando um rascunho e apagado. */
    static final String EVENTO_EXCLUSAO = "EXCLUSAO_PECA";

    private final DocumentoRepositorio documentos;
    private final VersaoDocumentoRepositorio versoes;
    private final VerificadorDeAcessoAoProcesso verificadorAcesso;
    private final RegistradorDeAuditoria auditoria;
    private final Relogio relogio;

    public EdicaoDocumentoService(DocumentoRepositorio documentos,
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
    @Transactional
    public DocumentoGerado executar(DocumentosUseCases.EditarDocumento.Comando comando) {
        DocumentoGerado documento = documentos.carregar(comando.documentoId());
        exigirAcesso(documento, comando.autorOab());
        documento.editar(comando.conteudo());
        DocumentoGerado salvo = documentos.salvar(documento);
        versoes.registrar(new VersaoDocumento(null, salvo.getId(), salvo.getVersaoAtual(), salvo.getConteudo(),
                OrigemVersao.EDICAO, comando.autorOab(), comando.autorNome(), relogio.agora(),
                comando.resumo() == null || comando.resumo().isBlank()
                        ? "Edição manual do texto"
                        : comando.resumo().trim()));
        return salvo;
    }

    @Override
    @Transactional
    public DocumentoGerado executar(DocumentosUseCases.RestaurarVersao.Comando comando) {
        DocumentoGerado documento = documentos.carregar(comando.documentoId());
        exigirAcesso(documento, comando.autorOab());
        VersaoDocumento alvo = versoes.versao(comando.documentoId(), comando.numero())
                .orElseThrow(() -> new NoSuchElementException(
                        "versão " + comando.numero() + " não encontrada na peça " + comando.documentoId()));
        documento.restaurar(alvo);
        DocumentoGerado salvo = documentos.salvar(documento);
        versoes.registrar(new VersaoDocumento(null, salvo.getId(), salvo.getVersaoAtual(), salvo.getConteudo(),
                OrigemVersao.RESTAURACAO, comando.autorOab(), comando.autorNome(), relogio.agora(),
                "Texto da versão " + alvo.numero() + " restaurado"));
        return salvo;
    }

    /**
     * Apaga a peca e as versoes. A auditoria de acesso guarda o registro da
     * exclusao (quem, quando, qual peca), porque a peca em si deixa de existir.
     */
    @Override
    @Transactional
    public void executar(DocumentosUseCases.ExcluirDocumento.Comando comando) {
        DocumentoGerado documento = documentos.carregar(comando.documentoId());
        exigirAcesso(documento, comando.autorOab());
        documento.exigirExclusaoPermitida(comando.autorOab(), comando.chefe());
        versoes.excluirDoDocumento(documento.getId());
        documentos.excluir(documento.getId());
        auditoria.registrar(EventoDeAuditoria.gestao(EVENTO_EXCLUSAO, documento.getNumeroProcesso(),
                comando.autorOab(), comando.usuarioId(), comando.usuarioNome(), relogio.agora(),
                "Rascunho #" + documento.getId() + " (" + documento.getTipo().rotulo() + ", v"
                        + documento.getVersaoAtual() + ") excluído"));
    }

    /**
     * Mudar o texto exige poder le-lo: a mesma politica do Proxy de sigilo
     * (OAB habilitada na peca ou acesso ativo ao processo). O chefe sem OAB
     * habilitada tambem e barrado - sigilo nao tem excecao por cargo.
     */
    private void exigirAcesso(DocumentoGerado documento, String oab) {
        if (!PoliticaDeLeituraDosAutos.podeLer(documento, oab, verificadorAcesso, relogio.hoje())) {
            throw new ProxyDeAcesso.AcessoNegadoException("documento " + documento.getId()
                    + " está em segredo de justiça e a OAB " + oab + " não está habilitada nos autos");
        }
    }
}
