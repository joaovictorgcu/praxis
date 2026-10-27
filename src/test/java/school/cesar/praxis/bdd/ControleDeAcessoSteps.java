package school.cesar.praxis.bdd;

import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import school.cesar.praxis.application.port.in.AcessoUseCases;
import school.cesar.praxis.application.port.in.AnexosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.application.port.out.AcessoAoProcessoRepositorio;
import school.cesar.praxis.domain.acesso.AcessoAoProcesso;
import school.cesar.praxis.domain.anexo.ArquivoAnexo;
import school.cesar.praxis.domain.compartilhado.ProxyDeAcesso;
import school.cesar.praxis.domain.compartilhado.ResultadoAcesso;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.infrastructure.notificacao.NotificadorPainel;
import school.cesar.praxis.presentation.rest.AnexoRestController;
import school.cesar.praxis.presentation.web.seguranca.SessaoExigidaException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Steps do Cucumber para o controle de acesso aos autos sigilosos:
 * solicitacao, concessao, revogacao, expiracao e auditoria. O processo e o
 * anexo do contexto sao criados aqui mesmo, como em HonorariosFinanceiroSteps,
 * para esta classe nao depender de campo privado de outra classe de steps.
 */
public class ControleDeAcessoSteps {

    @Autowired
    private ProcessosUseCases.CadastrarProcesso cadastrarProcesso;
    @Autowired
    private AnexosUseCases.AnexarArquivo anexar;
    @Autowired
    private AnexosUseCases.BaixarAnexo baixarAnexo;
    @Autowired
    private AcessoUseCases.ConcederAcesso concederAcesso;
    @Autowired
    private AcessoUseCases.RevogarAcesso revogarAcesso;
    @Autowired
    private AcessoUseCases.ListarAcessosDoProcesso listarAcessos;
    @Autowired
    private AcessoUseCases.CriarSolicitacao criarSolicitacao;
    @Autowired
    private AcessoUseCases.AprovarSolicitacao aprovarSolicitacao;
    @Autowired
    private AcessoUseCases.NegarSolicitacao negarSolicitacao;
    @Autowired
    private AcessoUseCases.ConsultarAuditoria consultarAuditoria;
    @Autowired
    private AnexoRestController anexoRestController;
    @Autowired
    private AcessoAoProcessoRepositorio acessoAoProcessoRepositorio;
    @Autowired
    private NotificadorPainel painel;

    private static final String OAB_RESPONSAVEL = "PE12345";

    private String numeroProcesso;
    private Long anexoId;
    private AcessoUseCases.ItemSolicitacao solicitacao;
    private RuntimeException falhaEsperada;

    @Before
    public void limparEstado() {
        numeroProcesso = null;
        anexoId = null;
        solicitacao = null;
        falhaEsperada = null;
    }

    @Dado("um processo em segredo de justica para controle de acesso")
    public void umProcessoEmSegredoDeJusticaParaControleDeAcesso() {
        numeroProcesso = "0006666-66.2026.8.17.0099";
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                numeroProcesso, "Cliente Sigiloso Ltda.", "Recife", true,
                "Ana Souza", "ana@praxis.adv.br", OAB_RESPONSAVEL));

        AnexosUseCases.ItemAnexo anexo = anexar.executar(new AnexosUseCases.AnexarArquivo.Comando(
                numeroProcesso, "laudo.pdf", "application/pdf",
                "laudo sigiloso".getBytes(), "Laudo pericial", OAB_RESPONSAVEL));
        anexoId = anexo.id();
    }

    // --- Solicitacao ---

    @Quando("a OAB {string} solicita acesso ao processo com justificativa {string}")
    public void aOabSolicitaAcessoAoProcesso(String oab, String justificativa) {
        solicitacao = criarSolicitacao.executar(new AcessoUseCases.CriarSolicitacao.Comando(
                numeroProcesso, oab, justificativa));
    }

    @Dado("que a OAB {string} solicitou acesso ao processo com justificativa {string}")
    public void queAOabSolicitouAcesso(String oab, String justificativa) {
        aOabSolicitaAcessoAoProcesso(oab, justificativa);
    }

    @Quando("a OAB {string} tenta solicitar acesso de novo ao processo com justificativa {string}")
    public void aOabTentaSolicitarDeNovo(String oab, String justificativa) {
        falhaEsperada = assertThrows(IllegalArgumentException.class, () ->
                criarSolicitacao.executar(new AcessoUseCases.CriarSolicitacao.Comando(numeroProcesso, oab, justificativa)));
    }

    @Quando("o chefe aprova a solicitacao")
    public void oChefeAprovaASolicitacao() {
        solicitacao = aprovarSolicitacao.executar(new AcessoUseCases.AprovarSolicitacao.Comando(
                solicitacao.id(), "PE00001", null));
    }

    @E("o chefe nega a solicitacao com motivo {string}")
    public void oChefeNegaASolicitacao(String motivo) {
        solicitacao = negarSolicitacao.executar(new AcessoUseCases.NegarSolicitacao.Comando(
                solicitacao.id(), "PE00001", motivo));
    }

    @Entao("a solicitacao deve estar com status {string}")
    public void aSolicitacaoDeveEstarComStatus(String statusEsperado) {
        assertEquals(statusEsperado, solicitacao.status());
    }

    @E("o chefe deve ser notificado da nova solicitacao")
    public void oChefeDeveSerNotificadoDaNovaSolicitacao() {
        assertTrue(painel.getEntregues().stream()
                        .anyMatch(n -> numeroProcesso.equals(n.numeroProcesso()) && n.assunto().contains("Nova solicitação")),
                "chefe nao foi notificado da nova solicitacao");
    }

    @E("o solicitante deve ser notificado da aprovacao")
    public void oSolicitanteDeveSerNotificadoDaAprovacao() {
        assertTrue(painel.getEntregues().stream()
                        .anyMatch(n -> numeroProcesso.equals(n.numeroProcesso()) && n.assunto().contains("aprovado")),
                "solicitante nao foi notificado da aprovacao");
    }

    @E("o solicitante deve ser notificado da negativa")
    public void oSolicitanteDeveSerNotificadoDaNegativa() {
        assertTrue(painel.getEntregues().stream()
                        .anyMatch(n -> numeroProcesso.equals(n.numeroProcesso()) && n.assunto().contains("negado")),
                "solicitante nao foi notificado da negativa");
    }

    // --- Acesso temporario ---

    /**
     * O caso de uso sempre concede a partir de "hoje" (relogio do projeto), entao
     * um acesso ja vencido so existe reconstituindo o agregado direto no
     * repositorio - exatamente como ele chegaria do banco depois de concedido
     * no passado e o tempo ter corrido sem o teste poder adiantar o relogio real.
     */
    @Dado("que a OAB {string} tem acesso ao processo que expirou ha {int} dia(s)")
    public void queAOabTemAcessoQueExpirou(String oab, int diasAtras) {
        LocalDate concedidoEm = LocalDate.now().minusDays(diasAtras + 10);
        LocalDate expiraEm = LocalDate.now().minusDays(diasAtras);
        acessoAoProcessoRepositorio.salvar(new AcessoAoProcesso(
                NumeroCnj.de(numeroProcesso), oab, OAB_RESPONSAVEL, concedidoEm, expiraEm));
    }

    @Dado("que a OAB {string} tem acesso ao processo que expira em {int} dias")
    public void queAOabTemAcessoQueExpiraEm(String oab, int dias) {
        concederAcesso.executar(new AcessoUseCases.ConcederAcesso.Comando(
                numeroProcesso, oab, OAB_RESPONSAVEL, LocalDate.now().plusDays(dias)));
    }

    @Entao("faltam {int} dias para o acesso da OAB {string} expirar")
    public void faltamDiasParaOAcessoExpirar(int diasEsperados, String oab) {
        AcessoUseCases.ItemAcesso acesso = listarAcessos.executar(numeroProcesso).stream()
                .filter(a -> a.oab().equals(oab))
                .findFirst()
                .orElseThrow(() -> new AssertionError("acesso da OAB " + oab + " nao encontrado"));
        assertEquals(Long.valueOf(diasEsperados), acesso.diasParaExpirar());
    }

    // --- Regra do ultimo acesso ---

    @Quando("eu tento revogar o acesso do responsavel pelo processo")
    public void euTentoRevogarOAcessoDoResponsavel() {
        AcessoUseCases.ItemAcesso acessoDoResponsavel = listarAcessos.executar(numeroProcesso).stream()
                .filter(a -> a.oab().equals(OAB_RESPONSAVEL))
                .findFirst()
                .orElseThrow(() -> new AssertionError("acesso automatico do responsavel nao encontrado"));
        falhaEsperada = assertThrows(IllegalStateException.class, () ->
                revogarAcesso.executar(new AcessoUseCases.RevogarAcesso.Comando(
                        acessoDoResponsavel.id(), "revogacao de teste", "PE00001")));
    }

    @Entao("a operacao deve ser recusada por exigir ao menos um acesso ativo")
    public void aOperacaoDeveSerRecusadaPorExigirAoMenosUmAcessoAtivo() {
        assertNotNull(falhaEsperada, "a revogacao foi aceita");
        assertTrue(falhaEsperada.getMessage().contains("acesso ativo"), falhaEsperada.getMessage());
    }

    @Entao("a operacao deve ser recusada por solicitacao pendente duplicada")
    public void aOperacaoDeveSerRecusadaPorSolicitacaoPendenteDuplicada() {
        assertNotNull(falhaEsperada, "a segunda solicitacao foi aceita");
        assertTrue(falhaEsperada.getMessage().contains("pendente"), falhaEsperada.getMessage());
    }

    // --- Leitura, pelo Proxy ---

    @Entao("o acesso da OAB {string} ao anexo deve ser permitido")
    public void aLeituraDoAnexoDeveSerPermitida(String oab) {
        ArquivoAnexo lido = baixarAnexo.executar(anexoId, oab);
        assertNotNull(lido);
    }

    @E("o acesso da OAB {string} ao anexo deve ser negado")
    public void aLeituraDoAnexoDeveSerNegada(String oab) {
        assertThrows(ProxyDeAcesso.AcessoNegadoException.class, () -> baixarAnexo.executar(anexoId, oab));
    }

    @Quando("eu tento ler o anexo do processo com a OAB {string}")
    public void euTentoLerOAnexoComAOab(String oab) {
        assertThrows(ProxyDeAcesso.AcessoNegadoException.class, () -> baixarAnexo.executar(anexoId, oab));
    }

    // --- Auditoria ---

    @Entao("a tentativa deve constar na auditoria como negada")
    public void aTentativaDeveConstarNaAuditoriaComoNegada() {
        List<AcessoUseCases.ItemAuditoria> eventos = consultarAuditoria.executar(
                new AcessoUseCases.ConsultarAuditoria.Filtro(numeroProcesso, null, ResultadoAcesso.NEGADO, null, null));
        assertFalse(eventos.isEmpty(), "nenhuma tentativa negada registrada na auditoria");
        assertTrue(eventos.stream().anyMatch(e -> "arquivo".equals(e.conteudoTipo()) && anexoId.equals(e.conteudoId())));
    }

    // --- Brecha do acesso anonimo ---

    @Entao("o download do anexo sem login deve ser recusado")
    public void oDownloadDoAnexoSemLoginDeveSerRecusado() {
        assertThrows(SessaoExigidaException.class, () -> anexoRestController.baixar(anexoId, null));
    }
}
