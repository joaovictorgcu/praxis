package school.cesar.praxis.bdd;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.domain.prazo.AlertaPrazo;
import school.cesar.praxis.domain.prazo.ExecucaoVarredura;
import school.cesar.praxis.domain.prazo.OrigemVarredura;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.prazo.PrerrogativaPrazo;
import school.cesar.praxis.domain.prazo.RegimeContagem;
import school.cesar.praxis.domain.prazo.SituacaoPrazo;
import school.cesar.praxis.fixtures.RelogioAjustavel;
import school.cesar.praxis.infrastructure.persistence.repository.PrazoEventoJpaRepository;
import school.cesar.praxis.infrastructure.persistence.repository.VarreduraExecucaoJpaRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Steps do ciclo de vida do prazo. O processo do contexto e criado aqui mesmo,
 * como em {@code HonorariosFinanceiroSteps}, para nao depender de campo
 * privado de outra classe de steps. "Hoje" e fixado pelo {@link RelogioAjustavel}
 * e volta ao relogio do sistema depois de cada cenario.
 */
public class CicloDeVidaPrazoSteps {

    private static final String PROCESSO = "0007777-77.2026.8.17.0077";
    private static final PrazosUseCases.Autor ANA = new PrazosUseCases.Autor("PE12345", "Ana Souza", false);
    private static final PrazosUseCases.Autor CHEFE = new PrazosUseCases.Autor("PE00001", "Carla Mendes", true);

    @Autowired
    private ProcessosUseCases.CadastrarProcesso cadastrarProcesso;
    @Autowired
    private PrazosUseCases.AbrirPrazo abrirPrazo;
    @Autowired
    private PrazosUseCases.SuspenderPrazo suspenderPrazo;
    @Autowired
    private PrazosUseCases.RetomarPrazo retomarPrazo;
    @Autowired
    private PrazosUseCases.DilatarPrazo dilatarPrazo;
    @Autowired
    private PrazosUseCases.CancelarPrazo cancelarPrazo;
    @Autowired
    private PrazosUseCases.ReabrirPrazo reabrirPrazo;
    @Autowired
    private PrazosUseCases.ConsultarPrazo consultarPrazo;
    @Autowired
    private PrazosUseCases.VarrerPrazos varrerPrazos;
    @Autowired
    private PrazosUseCases.ListarVarreduras listarVarreduras;
    @Autowired
    private PrazoEventoJpaRepository eventosJpa;
    @Autowired
    private VarreduraExecucaoJpaRepository varredurasJpa;
    @Autowired
    private RelogioAjustavel relogio;

    private Prazo prazo;
    private final List<AlertaPrazo> alertas = new ArrayList<>();
    private RuntimeException falha;

    @Before
    public void limparEstado() {
        eventosJpa.deleteAll();
        varredurasJpa.deleteAll();
        prazo = null;
        alertas.clear();
        falha = null;
    }

    @After
    public void relogioDoSistema() {
        relogio.voltarAoSistema();
    }

    // --- Contexto ---

    @Dado("um processo para o ciclo de vida do prazo")
    public void umProcessoParaOCicloDeVida() {
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                PROCESSO, "Joana Ferreira Lima", "Recife", false,
                "Ana Souza", "ana@praxis.adv.br", "PE12345"));
    }

    @Dado("que hoje e {string}")
    public void queHojeE(String dia) {
        relogio.fixar(LocalDate.parse(dia));
    }

    @Dado("um prazo fatal de {int} dias uteis intimado em {string} no ciclo de vida")
    public void umPrazoFatal(int dias, String intimacao) {
        prazo = abrirPrazo.executar(new PrazosUseCases.AbrirPrazo.Comando(PROCESSO, "Embargos de declaração",
                LocalDate.parse(intimacao), dias, true, RegimeContagem.DIAS_UTEIS, PrerrogativaPrazo.NENHUMA, ANA));
    }

    @Dado("um prazo fatal de {int} dias uteis intimado em {string} com prerrogativa {string}")
    public void umPrazoComPrerrogativa(int dias, String intimacao, String prerrogativa) {
        prazo = abrirPrazo.executar(new PrazosUseCases.AbrirPrazo.Comando(PROCESSO, "Contestação",
                LocalDate.parse(intimacao), dias, true, RegimeContagem.DIAS_UTEIS,
                PrerrogativaPrazo.valueOf(prerrogativa), ANA));
    }

    // --- Acoes ---

    @Quando("o prazo e suspenso desde {string} por {string}")
    public void oPrazoESuspenso(String inicio, String motivo) {
        prazo = suspenderPrazo.executar(new PrazosUseCases.SuspenderPrazo.Comando(
                prazo.getId(), LocalDate.parse(inicio), motivo, ANA));
    }

    @Quando("o prazo e retomado em {string}")
    public void oPrazoERetomado(String retomada) {
        prazo = retomarPrazo.executar(new PrazosUseCases.RetomarPrazo.Comando(
                prazo.getId(), LocalDate.parse(retomada), ANA));
    }

    @Quando("o prazo e dilatado em {int} dias por {string}")
    public void oPrazoEDilatado(int dias, String fundamento) {
        prazo = dilatarPrazo.executar(new PrazosUseCases.DilatarPrazo.Comando(prazo.getId(), dias, fundamento, ANA));
    }

    @Quando("eu tento dilatar o prazo em {int} dias por {string}")
    public void euTentoDilatar(int dias, String fundamento) {
        falha = assertThrows(RuntimeException.class, () -> dilatarPrazo.executar(
                new PrazosUseCases.DilatarPrazo.Comando(prazo.getId(), dias, fundamento, ANA)));
    }

    @Quando("a varredura manual roda em {string} pedida por {string}")
    public void aVarreduraManualRoda(String dia, String solicitante) {
        alertas.addAll(varrerPrazos.executar(new PrazosUseCases.VarrerPrazos.Comando(
                LocalDate.parse(dia), OrigemVarredura.MANUAL, solicitante)).alertas());
    }

    @Quando("o advogado de OAB {string} tenta cancelar o prazo por {string}")
    public void outroAdvogadoTentaCancelar(String oab, String motivo) {
        falha = assertThrows(RuntimeException.class, () -> cancelarPrazo.executar(new PrazosUseCases.CancelarPrazo
                .Comando(prazo.getId(), motivo, new PrazosUseCases.Autor(oab, "Outro advogado", false))));
    }

    @Quando("o responsavel cancela o prazo por {string}")
    public void oResponsavelCancela(String motivo) {
        prazo = cancelarPrazo.executar(new PrazosUseCases.CancelarPrazo.Comando(prazo.getId(), motivo, ANA));
    }

    @Quando("o chefe reabre o prazo por {string}")
    public void oChefeReabre(String motivo) {
        prazo = reabrirPrazo.executar(new PrazosUseCases.ReabrirPrazo.Comando(prazo.getId(), motivo, CHEFE));
    }

    // --- Verificacoes (sempre relidas do banco) ---

    @Entao("a situacao do prazo deve ser {string}")
    public void aSituacaoDeveSer(String situacao) {
        assertEquals(SituacaoPrazo.valueOf(situacao), detalhe().situacao());
    }

    @E("o saldo congelado deve ser de {int} dias")
    public void oSaldoCongelado(int dias) {
        assertEquals(dias, detalhe().diasRestantes());
    }

    @Entao("o vencimento vigente deve ser {string}")
    public void oVencimentoVigente(String vencimento) {
        assertEquals(LocalDate.parse(vencimento), detalhe().vencimento());
    }

    @E("o vencimento original deve continuar {string}")
    public void oVencimentoOriginal(String vencimento) {
        assertEquals(LocalDate.parse(vencimento), detalhe().vencimentoOriginal());
    }

    @E("a trilha do prazo deve ter os eventos {string}")
    public void aTrilhaDeveTer(String esperados) {
        assertEquals(esperados, String.join(", ",
                detalhe().eventos().stream().map(evento -> evento.tipo().name()).toList()));
    }

    @E("o prazo nao deve ter alertas emitidos")
    public void semAlertasEmitidos() {
        assertTrue(detalhe().alertasEmitidos().isEmpty());
    }

    @Entao("nenhum alerta deve ser emitido no ciclo de vida")
    public void nenhumAlerta() {
        assertTrue(alertas.isEmpty(), "alertas inesperados: " + alertas);
    }

    @E("a ultima varredura registrada deve ter avaliado {int} prazos e emitido {int} alertas")
    public void aUltimaVarreduraContou(int avaliados, int emitidos) {
        ExecucaoVarredura ultima = listarVarreduras.executar(1).get(0);
        assertEquals(avaliados, ultima.prazosAvaliados());
        assertEquals(emitidos, ultima.alertasEmitidos());
        assertEquals(OrigemVarredura.MANUAL, ultima.origem());
    }

    @E("a ultima varredura registrada deve ter sido pedida por {string}")
    public void aUltimaVarreduraFoiPedidaPor(String solicitante) {
        assertEquals(solicitante, listarVarreduras.executar(1).get(0).solicitante());
    }

    @Entao("a operacao no prazo deve ser recusada com a mensagem contendo {string}")
    public void operacaoRecusada(String trecho) {
        assertNotNull(falha, "a operação deveria ter sido recusada");
        assertTrue(falha.getMessage().contains(trecho), "mensagem: " + falha.getMessage());
    }

    private PrazosUseCases.ConsultarPrazo.DetalhePrazo detalhe() {
        return consultarPrazo.executar(prazo.getId());
    }
}
