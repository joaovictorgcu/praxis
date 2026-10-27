package school.cesar.praxis.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.cesar.praxis.domain.honorario.*;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de unidade do controle financeiro de honorarios: rateio de centavos
 * no parcelamento, transicoes de status de parcela/contrato, encargos de
 * atraso (multa e juros) e quota litis em duas fases (incluindo o limite do
 * art. 50 do Codigo de Etica da OAB). O fluxo completo, ponta a ponta, esta
 * coberto pelos cenarios de honorarios_financeiro.feature; aqui o foco e a
 * regra de negocio isolada.
 */
class ControleFinanceiroHonorariosTest {

    private final NumeroCnj processo = NumeroCnj.de("0001234-56.2026.8.17.0001");

    // --- Rateio de centavos ---

    @Test
    @DisplayName("rateio de centavos: a diferenca vai para a ultima parcela e a soma bate exato com o valor contratado")
    void rateioDeCentavosNaUltimaParcela() {
        ContratoHonorario contrato = new ContratoHonorario(
                processo, LocalDate.of(2026, 1, 10),
                BaseCalculo.fixo(new BigDecimal("1000.00")),
                new HonorarioFixo(), 3, LocalDate.of(2026, 2, 10));

        List<Parcela> parcelas = contrato.getParcelas();
        assertEquals(new BigDecimal("333.33"), parcelas.get(0).getValor());
        assertEquals(new BigDecimal("333.33"), parcelas.get(1).getValor());
        assertEquals(new BigDecimal("333.34"), parcelas.get(2).getValor());

        BigDecimal soma = parcelas.stream().map(Parcela::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("1000.00").compareTo(soma));
    }

    @Test
    @DisplayName("rateio de centavos: vencimentos das parcelas avancam mes a mes a partir do primeiro vencimento")
    void vencimentosAvancamMensalmente() {
        ContratoHonorario contrato = new ContratoHonorario(
                processo, LocalDate.of(2026, 1, 10),
                BaseCalculo.fixo(new BigDecimal("300.00")),
                new HonorarioFixo(), 3, LocalDate.of(2026, 2, 10));

        List<Parcela> parcelas = contrato.getParcelas();
        assertEquals(LocalDate.of(2026, 2, 10), parcelas.get(0).getVencimento());
        assertEquals(LocalDate.of(2026, 3, 10), parcelas.get(1).getVencimento());
        assertEquals(LocalDate.of(2026, 4, 10), parcelas.get(2).getVencimento());
    }

    // --- Transicoes de status da parcela ---

    @Test
    @DisplayName("status da parcela: pendente antes do vencimento e sem pagamento")
    void parcelaPendente() {
        Parcela parcela = new Parcela(1, LocalDate.of(2026, 3, 1), new BigDecimal("100.00"));
        assertEquals(StatusParcela.PENDENTE, parcela.status(LocalDate.of(2026, 2, 1)));
    }

    @Test
    @DisplayName("status da parcela: parcialmente paga com pagamento parcial antes do vencimento")
    void parcelaParcialmentePaga() {
        Parcela parcela = new Parcela(1, LocalDate.of(2026, 3, 1), new BigDecimal("100.00"));
        parcela.registrarPagamento(new BigDecimal("40.00"), LocalDate.of(2026, 2, 1));
        assertEquals(StatusParcela.PARCIALMENTE_PAGA, parcela.status(LocalDate.of(2026, 2, 15)));
    }

    @Test
    @DisplayName("status da parcela: paga quando o saldo chega a zero")
    void parcelaPaga() {
        Parcela parcela = new Parcela(1, LocalDate.of(2026, 3, 1), new BigDecimal("100.00"));
        parcela.registrarPagamento(new BigDecimal("100.00"), LocalDate.of(2026, 2, 15));
        assertEquals(StatusParcela.PAGA, parcela.status(LocalDate.of(2026, 2, 20)));
    }

    @Test
    @DisplayName("status da parcela: em atraso quando hoje e depois do vencimento e ainda ha saldo")
    void parcelaEmAtraso() {
        Parcela parcela = new Parcela(1, LocalDate.of(2026, 3, 1), new BigDecimal("100.00"));
        assertEquals(StatusParcela.EM_ATRASO, parcela.status(LocalDate.of(2026, 3, 2)));
    }

    @Test
    @DisplayName("status da parcela: paga tem prioridade sobre atraso mesmo se o pagamento veio depois do vencimento")
    void parcelaPagaAposVencimentoNaoFicaEmAtraso() {
        Parcela parcela = new Parcela(1, LocalDate.of(2026, 3, 1), new BigDecimal("100.00"));
        parcela.registrarPagamento(new BigDecimal("100.00"), LocalDate.of(2026, 3, 10));
        assertEquals(StatusParcela.PAGA, parcela.status(LocalDate.of(2026, 3, 10)));
    }

    @Test
    @DisplayName("pagamento acima do saldo da parcela e recusado pelo dominio")
    void pagamentoAcimaDoSaldoERecusado() {
        Parcela parcela = new Parcela(1, LocalDate.of(2026, 3, 1), new BigDecimal("100.00"));
        parcela.registrarPagamento(new BigDecimal("60.00"), LocalDate.of(2026, 2, 1));

        IllegalArgumentException falha = assertThrows(IllegalArgumentException.class,
                () -> parcela.registrarPagamento(new BigDecimal("50.00"), LocalDate.of(2026, 2, 10)));
        assertTrue(falha.getMessage().contains("maior que o saldo"));
    }

    @Test
    @DisplayName("pagar uma parcela ja paga e recusado por falta de saldo")
    void pagarParcelaJaPagaERecusado() {
        Parcela parcela = new Parcela(1, LocalDate.of(2026, 3, 1), new BigDecimal("100.00"));
        parcela.registrarPagamento(new BigDecimal("100.00"), LocalDate.of(2026, 2, 1));

        assertThrows(IllegalArgumentException.class,
                () -> parcela.registrarPagamento(new BigDecimal("0.01"), LocalDate.of(2026, 2, 2)));
    }

    // --- Transicoes de status do contrato ---

    @Test
    @DisplayName("status do contrato: em aberto sem nenhum pagamento e dentro do prazo")
    void contratoEmAberto() {
        ContratoHonorario contrato = new ContratoHonorario(
                processo, LocalDate.of(2026, 1, 1), BaseCalculo.fixo(new BigDecimal("900.00")),
                new HonorarioFixo(), 3, LocalDate.of(2026, 3, 1));

        assertEquals(StatusContrato.EM_ABERTO, contrato.status(LocalDate.of(2026, 1, 5)));
    }

    @Test
    @DisplayName("status do contrato: parcialmente pago com uma parcela quitada e as demais em aberto")
    void contratoParcialmentePago() {
        ContratoHonorario contrato = new ContratoHonorario(
                processo, LocalDate.of(2026, 1, 1), BaseCalculo.fixo(new BigDecimal("900.00")),
                new HonorarioFixo(), 3, LocalDate.of(2026, 3, 1));

        contrato.registrarPagamento(1, new BigDecimal("300.00"), LocalDate.of(2026, 2, 20));

        assertEquals(StatusContrato.PARCIALMENTE_PAGO, contrato.status(LocalDate.of(2026, 2, 25)));
    }

    @Test
    @DisplayName("status do contrato: quitado quando todas as parcelas estao pagas")
    void contratoQuitado() {
        ContratoHonorario contrato = new ContratoHonorario(
                processo, LocalDate.of(2026, 1, 1), BaseCalculo.fixo(new BigDecimal("900.00")),
                new HonorarioFixo(), 3, LocalDate.of(2026, 3, 1));

        for (int numero = 1; numero <= 3; numero++) {
            contrato.registrarPagamento(numero, new BigDecimal("300.00"), LocalDate.of(2026, 2, 20));
        }

        assertEquals(StatusContrato.QUITADO, contrato.status(LocalDate.of(2026, 2, 25)));
    }

    @Test
    @DisplayName("status do contrato: em atraso pesa mais que pagamento parcial de outra parcela")
    void contratoEmAtrasoTemPrioridade() {
        ContratoHonorario contrato = new ContratoHonorario(
                processo, LocalDate.of(2026, 1, 1), BaseCalculo.fixo(new BigDecimal("900.00")),
                new HonorarioFixo(), 3, LocalDate.of(2026, 1, 15));

        contrato.registrarPagamento(1, new BigDecimal("300.00"), LocalDate.of(2026, 1, 10));

        assertEquals(StatusContrato.EM_ATRASO, contrato.status(LocalDate.of(2026, 4, 1)));
    }

    @Test
    @DisplayName("registrar pagamento em parcela inexistente e recusado")
    void pagamentoEmParcelaInexistenteERecusado() {
        ContratoHonorario contrato = new ContratoHonorario(
                processo, LocalDate.of(2026, 1, 1), BaseCalculo.fixo(new BigDecimal("300.00")),
                new HonorarioFixo(), 1, LocalDate.of(2026, 2, 1));

        assertThrows(IllegalArgumentException.class,
                () -> contrato.registrarPagamento(2, new BigDecimal("1.00"), LocalDate.of(2026, 1, 10)));
    }

    // --- Encargos por atraso: multa e juros ---

    @Test
    @DisplayName("multa de atraso: 2% sobre o saldo, uma vez so, independente dos dias de atraso")
    void multaDeAtrasoEDoisPorCentoSobreOSaldo() {
        Parcela parcela = new Parcela(1, LocalDate.of(2026, 3, 1), new BigDecimal("1000.00"));

        assertEquals(new BigDecimal("20.00"), parcela.multa(LocalDate.of(2026, 3, 2)));
        assertEquals(new BigDecimal("20.00"), parcela.multa(LocalDate.of(2026, 3, 31)));
    }

    @Test
    @DisplayName("parcela em dia nao tem multa nem juros")
    void parcelaEmDiaNaoTemEncargos() {
        Parcela parcela = new Parcela(1, LocalDate.of(2026, 3, 1), new BigDecimal("1000.00"));

        assertEquals(BigDecimal.ZERO.setScale(2), parcela.multa(LocalDate.of(2026, 2, 1)));
        assertEquals(BigDecimal.ZERO.setScale(2), parcela.juros(LocalDate.of(2026, 2, 1)));
    }

    @Test
    @DisplayName("juros de atraso: 1% ao mes pro rata die, escalando com os dias corridos de atraso")
    void jurosEscalamComOsDiasDeAtraso() {
        Parcela parcela = new Parcela(1, LocalDate.of(2026, 3, 1), new BigDecimal("3000.00"));

        // taxa diaria = 1%/30; saldo 3000 => 1 real por dia de atraso
        BigDecimal jurosDezDias = parcela.juros(LocalDate.of(2026, 3, 11));
        BigDecimal jurosVinteDias = parcela.juros(LocalDate.of(2026, 3, 21));

        assertEquals(new BigDecimal("10.00"), jurosDezDias);
        assertEquals(new BigDecimal("20.00"), jurosVinteDias);
        assertTrue(jurosVinteDias.compareTo(jurosDezDias) > 0);
    }

    @Test
    @DisplayName("valor atualizado da parcela vencida soma saldo, multa e juros")
    void valorAtualizadoSomaSaldoMultaEJuros() {
        Parcela parcela = new Parcela(1, LocalDate.of(2026, 3, 1), new BigDecimal("3000.00"));
        LocalDate hoje = LocalDate.of(2026, 3, 11);

        BigDecimal esperado = parcela.saldo().add(parcela.multa(hoje)).add(parcela.juros(hoje));
        assertEquals(esperado, parcela.valorAtualizado(hoje));
        assertEquals(new BigDecimal("3070.00"), parcela.valorAtualizado(hoje));
    }

    // --- Quota litis em duas fases ---

    @Test
    @DisplayName("quota litis nasce com valor estimado e aguardando o resultado da causa")
    void quotaLitisNasceAguardandoResultado() {
        ContratoHonorario contrato = new ContratoHonorario(
                processo, LocalDate.of(2026, 1, 1),
                BaseCalculo.quotaLitis(new BigDecimal("100000.00"), new BigDecimal("20")),
                new HonorarioQuotaLitis(), 2, LocalDate.of(2026, 2, 1));

        assertTrue(contrato.aguardandoResultado());
        assertEquals(new BigDecimal("20000.00"), contrato.getValorContratado());
        assertTrue(contrato.getParcelas().isEmpty(), "nao deve gerar parcelas antes do resultado");
    }

    @Test
    @DisplayName("quota litis com exito recalcula o valor final sobre o proveito economico de verdade e gera as parcelas")
    void quotaLitisComExitoRecalculaSobreOProveitoReal() {
        ContratoHonorario contrato = new ContratoHonorario(
                processo, LocalDate.of(2026, 1, 1),
                BaseCalculo.quotaLitis(new BigDecimal("100000.00"), new BigDecimal("20")),
                new HonorarioQuotaLitis(), 2, LocalDate.of(2026, 2, 1));

        contrato.registrarResultado(new BigDecimal("150000.00"), new BigDecimal("5000.00"), new HonorarioQuotaLitis());

        assertFalse(contrato.aguardandoResultado());
        assertEquals(new BigDecimal("30000.00"), contrato.getValorContratado());
        assertEquals(2, contrato.getParcelas().size());
        BigDecimal soma = contrato.getParcelas().stream().map(Parcela::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("30000.00").compareTo(soma));
    }

    @Test
    @DisplayName("quota litis sem exito (valor obtido zero) encerra o contrato sem cobranca")
    void quotaLitisSemExitoEncerraSemCobranca() {
        ContratoHonorario contrato = new ContratoHonorario(
                processo, LocalDate.of(2026, 1, 1),
                BaseCalculo.quotaLitis(new BigDecimal("100000.00"), new BigDecimal("20")),
                new HonorarioQuotaLitis(), 2, LocalDate.of(2026, 2, 1));

        contrato.registrarResultado(BigDecimal.ZERO, BigDecimal.ZERO, new HonorarioQuotaLitis());

        assertFalse(contrato.aguardandoResultado());
        assertEquals(0, BigDecimal.ZERO.compareTo(contrato.getValorContratado()));
        assertTrue(contrato.getParcelas().isEmpty());
        assertEquals(StatusContrato.QUITADO, contrato.status(LocalDate.of(2026, 2, 1)));
    }

    @Test
    @DisplayName("resultado da causa so pode ser registrado uma vez")
    void resultadoSoPodeSerRegistradoUmaVez() {
        ContratoHonorario contrato = new ContratoHonorario(
                processo, LocalDate.of(2026, 1, 1),
                BaseCalculo.quotaLitis(new BigDecimal("100000.00"), new BigDecimal("20")),
                new HonorarioQuotaLitis(), 2, LocalDate.of(2026, 2, 1));
        contrato.registrarResultado(new BigDecimal("150000.00"), new BigDecimal("5000.00"), new HonorarioQuotaLitis());

        assertThrows(IllegalStateException.class,
                () -> contrato.registrarResultado(new BigDecimal("200000.00"), BigDecimal.ZERO, new HonorarioQuotaLitis()));
    }

    @Test
    @DisplayName("resultado da causa nao se aplica a contrato fixo ou por hora")
    void resultadoNaoSeAplicaAModalidadeDiferenteDeQuotaLitis() {
        ContratoHonorario contrato = new ContratoHonorario(
                processo, LocalDate.of(2026, 1, 1), BaseCalculo.fixo(new BigDecimal("1000.00")),
                new HonorarioFixo(), 1, LocalDate.of(2026, 2, 1));

        assertThrows(IllegalStateException.class,
                () -> contrato.registrarResultado(new BigDecimal("1000.00"), BigDecimal.ZERO, new HonorarioFixo()));
    }

    // --- Art. 50 do Codigo de Etica da OAB ---

    @Test
    @DisplayName("honorarios contratuais mais sucumbencia acima do valor obtido fere o art. 50 e e recusado")
    void honorariosAlemDoValorObtidoFeremArt50() {
        ContratoHonorario contrato = new ContratoHonorario(
                processo, LocalDate.of(2026, 1, 1),
                BaseCalculo.quotaLitis(new BigDecimal("100000.00"), new BigDecimal("20")),
                new HonorarioQuotaLitis(), 2, LocalDate.of(2026, 2, 1));

        IllegalArgumentException falha = assertThrows(IllegalArgumentException.class,
                () -> contrato.registrarResultado(new BigDecimal("10000.00"), new BigDecimal("9000.00"), new HonorarioQuotaLitis()));

        assertTrue(falha.getMessage().contains("art. 50"));
        assertTrue(contrato.aguardandoResultado(), "contrato recusado nao deve mudar de estado");
    }

    @Test
    @DisplayName("honorarios contratuais mais sucumbencia exatamente no limite do valor obtido sao aceitos")
    void honorariosNoLimiteExatoDoValorObtidoSaoAceitos() {
        ContratoHonorario contrato = new ContratoHonorario(
                processo, LocalDate.of(2026, 1, 1),
                BaseCalculo.quotaLitis(new BigDecimal("100000.00"), new BigDecimal("20")),
                new HonorarioQuotaLitis(), 1, LocalDate.of(2026, 2, 1));

        // 20% de 10000 = 2000 de honorarios contratuais; + 8000 de sucumbencia = 10000, exatamente o obtido
        contrato.registrarResultado(new BigDecimal("10000.00"), new BigDecimal("8000.00"), new HonorarioQuotaLitis());

        assertEquals(new BigDecimal("2000.00"), contrato.getValorContratado());
    }
}
