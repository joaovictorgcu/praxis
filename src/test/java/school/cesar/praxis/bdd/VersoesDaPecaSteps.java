package school.cesar.praxis.bdd;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.in.ModelosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.infrastructure.persistence.repository.AuditoriaAcessoJpaRepository;
import school.cesar.praxis.infrastructure.persistence.repository.DocumentoJpaRepository;
import school.cesar.praxis.infrastructure.persistence.repository.DocumentoVersaoJpaRepository;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Steps das versoes do texto da peca. Processo e modelo do contexto sao
 * criados aqui; o modelo do cenario e removido no fim, para nao sobrar entre
 * cenarios (mesma regra dos modelos de {@code PraxisSteps}).
 */
public class VersoesDaPecaSteps {

    private static final String PROCESSO = "0013579-56.2026.8.17.0001";
    private static final String RAFAEL = "PE45678";
    private static final String MODELO = "EMBARGOS_VERSOES";

    @Autowired
    private ProcessosUseCases.CadastrarProcesso cadastrarProcesso;
    @Autowired
    private ModelosUseCases.CadastrarModelo cadastrarModelo;
    @Autowired
    private ModelosUseCases.RemoverModelo removerModelo;
    @Autowired
    private DocumentosUseCases.GerarDocumento gerarDocumento;
    @Autowired
    private DocumentosUseCases.EditarDocumento editarDocumento;
    @Autowired
    private DocumentosUseCases.RegenerarDocumento regenerarDocumento;
    @Autowired
    private DocumentosUseCases.RestaurarVersao restaurarVersao;
    @Autowired
    private DocumentosUseCases.ExcluirDocumento excluirDocumento;
    @Autowired
    private DocumentosUseCases.ConsultarVersoes consultarVersoes;
    @Autowired
    private DocumentosUseCases.EnviarDocumentoParaRevisao enviarParaRevisao;
    @Autowired
    private DocumentosUseCases.BaixarDocumento baixarDocumento;
    @Autowired
    private DocumentoJpaRepository documentosJpa;
    @Autowired
    private DocumentoVersaoJpaRepository versoesJpa;
    @Autowired
    private AuditoriaAcessoJpaRepository auditoriaJpa;

    private Long modeloId;
    private DocumentoGerado peca;
    private DocumentosUseCases.ConsultarVersoes.Comparacao comparacao;
    private RuntimeException falha;

    @Before
    public void limparEstado() {
        versoesJpa.deleteAll();
        modeloId = null;
        peca = null;
        comparacao = null;
        falha = null;
    }

    @After
    public void removerModelo() {
        if (modeloId != null) {
            removerModelo.executar(modeloId);
        }
    }

    // --- Contexto ---

    @Dado("um processo para versoes de peca")
    public void umProcesso() {
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                PROCESSO, "Joana Ferreira Lima", "Recife", false,
                "Rafael Torres", "rafael.torres@praxis.adv.br", RAFAEL));
    }

    @E("o modelo de embargos de declaracao para versoes de peca")
    public void oModeloDeEmbargos() {
        modeloId = cadastrarModelo.executar(new ModelosUseCases.CadastrarModelo.Comando(
                MODELO, "Embargos de declaração", TipoDocumento.PECA_AVULSA, "Embargos de declaração",
                "DOS FATOS\nA decisão deixou de se pronunciar sobre {{omissao}}.",
                "DOS PEDIDOS\na) o acolhimento dos embargos, para suprir a omissão quanto a {{omissao}}.",
                true)).id();
    }

    // --- Acoes ---

    @Quando("eu gero a peca de embargos com a omissao {string}")
    @Dado("a peca de embargos gerada com a omissao {string}")
    public void gerarPeca(String omissao) {
        peca = gerarDocumento.executar(new DocumentosUseCases.GerarDocumento.Comando(
                PROCESSO, TipoDocumento.PECA_AVULSA, Map.of("omissao", omissao), RAFAEL, MODELO));
    }

    @Quando("eu edito a peca acrescentando a linha {string}")
    public void euEdito(String linha) {
        peca = editarDocumento.executar(new DocumentosUseCases.EditarDocumento.Comando(
                peca.getId(), peca.getConteudo() + "\n" + linha, "Acrescenta: " + linha, RAFAEL, "Rafael Torres"));
    }

    @Quando("eu regenero a peca com a omissao {string}")
    public void euRegenero(String omissao) {
        peca = regenerarDocumento.executar(new DocumentosUseCases.RegenerarDocumento.Comando(
                peca.getId(), Map.of("omissao", omissao), RAFAEL, "Rafael Torres"));
    }

    @Quando("eu restauro a versao {int}")
    public void euRestauro(int numero) {
        peca = restaurarVersao.executar(new DocumentosUseCases.RestaurarVersao.Comando(
                peca.getId(), numero, RAFAEL, "Rafael Torres"));
    }

    @Quando("eu comparo a versao {int} com a versao {int}")
    public void euComparo(int de, int para) {
        comparacao = consultarVersoes.comparar(peca.getId(), de, para, RAFAEL, null, "Rafael Torres");
    }

    @E("a peca de embargos foi enviada para revisao")
    public void enviadaParaRevisao() {
        peca = enviarParaRevisao.executar(new DocumentosUseCases.EnviarDocumentoParaRevisao.Comando(peca.getId()));
    }

    @Quando("eu tento editar a peca")
    public void euTentoEditar() {
        falha = assertThrows(IllegalStateException.class, () -> editarDocumento.executar(
                new DocumentosUseCases.EditarDocumento.Comando(peca.getId(), peca.getConteudo() + "\nx",
                        null, RAFAEL, "Rafael Torres")));
    }

    @Quando("o autor exclui o rascunho")
    public void oAutorExclui() {
        excluirDocumento.executar(new DocumentosUseCases.ExcluirDocumento.Comando(
                peca.getId(), RAFAEL, false, null, "Rafael Torres"));
    }

    @Quando("o autor tenta excluir a peca")
    public void oAutorTentaExcluir() {
        falha = assertThrows(IllegalStateException.class, () -> excluirDocumento.executar(
                new DocumentosUseCases.ExcluirDocumento.Comando(peca.getId(), RAFAEL, false, null, "Rafael Torres")));
    }

    // --- Verificacoes (relidas do banco) ---

    @Entao("a peca deve estar na versao {int}")
    public void aPecaNaVersao(int numero) {
        DocumentoGerado relida = baixarDocumento.executar(peca.getId(), RAFAEL);
        assertEquals(numero, relida.getVersaoAtual());
        List<DocumentosUseCases.ConsultarVersoes.ItemVersao> versoes = consultarVersoes.listar(peca.getId());
        assertEquals(numero, versoes.size());
        assertTrue(versoes.get(versoes.size() - 1).atual());
    }

    @E("a versao {int} deve ter origem {string}")
    public void aVersaoTemOrigem(int numero, String origem) {
        assertEquals(origem, consultarVersoes.listar(peca.getId()).get(numero - 1).origem().name());
    }

    @E("a peca deve guardar o modelo {string} e o campo {string} com {string}")
    public void aPecaGuardaOModelo(String modelo, String campo, String valor) {
        DocumentoGerado relida = baixarDocumento.executar(peca.getId(), RAFAEL);
        assertEquals(modelo, relida.getModeloCodigo());
        assertEquals(valor, relida.getCamposGeracao().get(campo));
    }

    @E("as origens das versoes devem ser {string}")
    public void asOrigens(String esperadas) {
        assertEquals(esperadas, String.join(", ", consultarVersoes.listar(peca.getId()).stream()
                .map(versao -> versao.origem().name()).toList()));
    }

    @E("o texto atual deve conter {string}")
    public void oTextoAtualContem(String trecho) {
        assertTrue(baixarDocumento.executar(peca.getId(), RAFAEL).getConteudo().contains(trecho));
    }

    @Entao("a comparacao deve ter {int} linha incluida e {int} removida")
    public void aComparacao(int incluidas, int removidas) {
        assertEquals(incluidas, comparacao.incluidas());
        assertEquals(removidas, comparacao.removidas());
    }

    @Entao("a mudanca de texto deve ser recusada")
    public void mudancaRecusada() {
        assertNotNull(falha);
        assertTrue(falha.getMessage().contains("editar"), falha.getMessage());
    }

    @Entao("a peca nao deve mais existir")
    public void aPecaNaoExiste() {
        assertFalse(documentosJpa.existsById(peca.getId()));
        assertTrue(versoesJpa.findByDocumentoIdOrderByNumeroAsc(peca.getId()).isEmpty());
    }

    @E("a auditoria deve registrar a exclusao da peca")
    public void auditoriaRegistraExclusao() {
        assertTrue(auditoriaJpa.findAll().stream().anyMatch(evento ->
                "EXCLUSAO_PECA".equals(evento.getTipoEvento()) && evento.getMotivo().contains("#" + peca.getId())));
    }

    @Entao("a exclusao deve ser recusada")
    public void exclusaoRecusada() {
        assertNotNull(falha);
        assertTrue(documentosJpa.existsById(peca.getId()));
    }
}
