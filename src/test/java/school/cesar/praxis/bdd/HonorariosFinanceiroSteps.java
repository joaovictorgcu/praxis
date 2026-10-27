package school.cesar.praxis.bdd;

import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import school.cesar.praxis.application.port.in.HonorariosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Steps do Cucumber para o controle financeiro de honorarios: parcelamento,
 * pagamentos, encargos de atraso e quota litis em duas fases. O processo do
 * contexto e criado aqui mesmo (nao reaproveita o de PraxisSteps) para esta
 * classe nao depender de campo privado de outra classe de steps.
 */
public class HonorariosFinanceiroSteps {

    @Autowired
    private ProcessosUseCases.CadastrarProcesso cadastrarProcesso;
    @Autowired
    private HonorariosUseCases.CadastrarContrato cadastrarContrato;
    @Autowired
    private HonorariosUseCases.ConsultarDetalheContrato consultarDetalhe;
    @Autowired
    private HonorariosUseCases.RegistrarPagamentoParcela registrarPagamento;
    @Autowired
    private HonorariosUseCases.RegistrarResultadoCausa registrarResultado;

    private String numeroProcesso;
    private HonorariosUseCases.DetalheContrato contrato;
    private RuntimeException falhaEsperada;

    @Before
    public void limparEstado() {
        numeroProcesso = null;
        contrato = null;
        falhaEsperada = null;
    }

    @Dado("um processo para honorarios financeiros")
    public void umProcessoParaHonorariosFinanceiros() {
        numeroProcesso = "0005555-55.2026.8.17.0099";
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                numeroProcesso, "Cliente Financeiro Ltda.", "Recife", false,
                "Ana Souza", "ana@praxis.adv.br", "PE12345"));
    }

    // --- Contratacao ---

    @Quando("eu contrato honorario fixo de {string} em {int} parcela(s)")
    public void euContratoHonorarioFixo(String valor, int parcelas) {
        contrato = contratarFixo(valor, parcelas, LocalDate.now());
    }

    @Dado("que contratei honorario fixo de {string} em {int} parcela(s)")
    public void queContrateiHonorarioFixo(String valor, int parcelas) {
        contrato = contratarFixo(valor, parcelas, LocalDate.now());
    }

    @Dado("que contratei honorario fixo de {string} em {int} parcela(s) com vencimento ha {int} dias")
    public void queContrateiComVencimentoAtrasado(String valor, int parcelas, int diasAtras) {
        contrato = contratarFixo(valor, parcelas, LocalDate.now().minusDays(diasAtras));
    }

    @Quando("eu contrato honorario quota litis de {int}% com causa estimada em {string} em {int} parcela(s)")
    public void euContratoQuotaLitis(int percentual, String valorCausa, int parcelas) {
        contrato = contratarQuotaLitis(percentual, valorCausa, parcelas);
    }

    @Dado("que contratei honorario quota litis de {int}% com causa estimada em {string} em {int} parcela(s)")
    public void queContrateiQuotaLitis(int percentual, String valorCausa, int parcelas) {
        contrato = contratarQuotaLitis(percentual, valorCausa, parcelas);
    }

    // --- Pagamentos ---

    @Quando("eu registro um pagamento de {string} na parcela {int}")
    public void euRegistroUmPagamento(String valor, int numero) {
        contrato = registrarPagamento.executar(new HonorariosUseCases.RegistrarPagamentoParcela.Comando(
                contrato.id(), numero, new BigDecimal(valor), LocalDate.now()));
    }

    @Quando("eu pago integralmente a parcela {int}")
    public void euPagoIntegralmente(int numero) {
        HonorariosUseCases.ItemParcela parcela = parcela(numero);
        BigDecimal saldo = parcela.valor().subtract(parcela.valorPago());
        contrato = registrarPagamento.executar(new HonorariosUseCases.RegistrarPagamentoParcela.Comando(
                contrato.id(), numero, saldo, LocalDate.now()));
    }

    @Quando("eu tento registrar um pagamento de {string} na parcela {int}")
    public void euTentoRegistrarUmPagamento(String valor, int numero) {
        falhaEsperada = assertThrows(IllegalArgumentException.class, () ->
                registrarPagamento.executar(new HonorariosUseCases.RegistrarPagamentoParcela.Comando(
                        contrato.id(), numero, new BigDecimal(valor), LocalDate.now())));
    }

    // --- Resultado da causa (quota litis) ---

    @Quando("eu registro o resultado da causa com valor obtido {string} e sucumbencia {string}")
    public void euRegistroOResultadoDaCausa(String valorObtido, String sucumbencia) {
        contrato = registrarResultado.executar(new HonorariosUseCases.RegistrarResultadoCausa.Comando(
                contrato.id(), new BigDecimal(valorObtido), new BigDecimal(sucumbencia)));
    }

    @Quando("eu tento registrar o resultado da causa com valor obtido {string} e sucumbencia {string}")
    public void euTentoRegistrarOResultadoDaCausa(String valorObtido, String sucumbencia) {
        falhaEsperada = assertThrows(IllegalArgumentException.class, () ->
                registrarResultado.executar(new HonorariosUseCases.RegistrarResultadoCausa.Comando(
                        contrato.id(), new BigDecimal(valorObtido), new BigDecimal(sucumbencia))));
    }

    // --- Verificacoes ---

    @Entao("a soma das parcelas deve ser igual a {string}")
    public void aSomaDasParcelasDeveSerIgualA(String valorEsperado) {
        BigDecimal soma = contrato.parcelas().stream()
                .map(HonorariosUseCases.ItemParcela::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal(valorEsperado).compareTo(soma));
    }

    @E("a parcela {int} deve valer {string}")
    public void aParcelaDeveValer(int numero, String valorEsperado) {
        assertEquals(0, new BigDecimal(valorEsperado).compareTo(parcela(numero).valor()));
    }

    @Entao("a parcela {int} deve estar com status {string}")
    public void aParcelaDeveEstarComStatus(int numero, String statusEsperado) {
        atualizarContrato();
        assertEquals(statusEsperado, parcela(numero).status());
    }

    @Entao("o contrato deve estar com status {string}")
    public void oContratoDeveEstarComStatus(String statusEsperado) {
        atualizarContrato();
        assertEquals(statusEsperado, contrato.status());
    }

    @Entao("a operacao deve ser recusada por saldo insuficiente")
    public void aOperacaoDeveSerRecusadaPorSaldoInsuficiente() {
        assertNotNull(falhaEsperada, "o pagamento foi aceito");
        assertTrue(falhaEsperada.getMessage().contains("maior que o saldo"),
                "motivo inesperado: " + falhaEsperada.getMessage());
    }

    @E("a multa da parcela {int} deve ser {string}")
    public void aMultaDaParcelaDeveSer(int numero, String valorEsperado) {
        assertEquals(0, new BigDecimal(valorEsperado).compareTo(parcela(numero).multa()));
    }

    @E("os juros da parcela {int} devem ser maiores que {string}")
    public void osJurosDaParcelaDevemSerMaioresQue(int numero, String valorMinimo) {
        assertTrue(parcela(numero).juros().compareTo(new BigDecimal(valorMinimo)) > 0,
                "juros deveriam ser maiores que " + valorMinimo + ", foram " + parcela(numero).juros());
    }

    @Entao("o contrato deve estar aguardando resultado")
    public void oContratoDeveEstarAguardandoResultado() {
        assertTrue(contrato.aguardandoResultado());
    }

    @E("o contrato nao deve mais estar aguardando resultado")
    public void oContratoNaoDeveMaisEstarAguardandoResultado() {
        assertFalse(contrato.aguardandoResultado());
    }

    @Entao("o valor contratado deve ser {string}")
    public void oValorContratadoDeveSer(String valorEsperado) {
        assertEquals(0, new BigDecimal(valorEsperado).compareTo(contrato.valorContratado()));
    }

    @Entao("a operacao deve ser recusada por violar o art. 50 do Codigo de Etica da OAB")
    public void aOperacaoDeveSerRecusadaPorViolarOArt50() {
        assertNotNull(falhaEsperada, "o resultado foi aceito");
        assertTrue(falhaEsperada.getMessage().contains("art. 50"),
                "motivo inesperado: " + falhaEsperada.getMessage());
    }

    // --- Apoio ---

    private HonorariosUseCases.DetalheContrato contratarFixo(String valor, int parcelas, LocalDate primeiroVencimento) {
        HonorariosUseCases.ItemContrato item = cadastrarContrato.executar(new HonorariosUseCases.CadastrarContrato.Comando(
                numeroProcesso, "FIXO", LocalDate.now(), new BigDecimal(valor), null, 0, null, null,
                parcelas, primeiroVencimento));
        return consultarDetalhe.executar(new HonorariosUseCases.ConsultarDetalheContrato.Comando(item.id()));
    }

    private HonorariosUseCases.DetalheContrato contratarQuotaLitis(int percentual, String valorCausa, int parcelas) {
        HonorariosUseCases.ItemContrato item = cadastrarContrato.executar(new HonorariosUseCases.CadastrarContrato.Comando(
                numeroProcesso, "QUOTA_LITIS", LocalDate.now(), null, null, 0,
                new BigDecimal(valorCausa), BigDecimal.valueOf(percentual), parcelas, LocalDate.now().plusDays(30)));
        return consultarDetalhe.executar(new HonorariosUseCases.ConsultarDetalheContrato.Comando(item.id()));
    }

    private void atualizarContrato() {
        contrato = consultarDetalhe.executar(new HonorariosUseCases.ConsultarDetalheContrato.Comando(contrato.id()));
    }

    private HonorariosUseCases.ItemParcela parcela(int numero) {
        return contrato.parcelas().stream()
                .filter(p -> p.numero() == numero)
                .findFirst()
                .orElseThrow(() -> new AssertionError("parcela " + numero + " nao encontrada"));
    }
}
