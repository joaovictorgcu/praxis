package school.cesar.praxis.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.cesar.praxis.domain.honorario.*;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculoDeHonorariosTest {

    private final NumeroCnj processo = NumeroCnj.de("0001234-56.2026.8.17.0001");

    @Test
    @DisplayName("honorario fixo cobra o valor combinado, independente de horas ou causa")
    void honorarioFixoCobraValorCombinado() {
        CalculoHonorarioStrategy fixo = new HonorarioFixo();

        assertEquals(new BigDecimal("5000"), fixo.calcular(BaseCalculo.fixo(new BigDecimal("5000"))));
        assertEquals("FIXO", fixo.modalidade());
    }

    @Test
    @DisplayName("honorario por hora multiplica o valor da hora pelas horas trabalhadas")
    void honorarioPorHoraMultiplicaValorPorHoras() {
        CalculoHonorarioStrategy porHora = new HonorarioPorHora();

        BigDecimal valor = porHora.calcular(BaseCalculo.porHora(new BigDecimal("150"), 10));

        assertEquals(new BigDecimal("1500"), valor);
        assertEquals("POR_HORA", porHora.modalidade());
    }

    @Test
    @DisplayName("quota litis cobra o percentual do proveito economico")
    void quotaLitisCobraPercentualDoProveito() {
        CalculoHonorarioStrategy quotaLitis = new HonorarioQuotaLitis();

        BigDecimal valor = quotaLitis.calcular(
                BaseCalculo.quotaLitis(new BigDecimal("100000"), new BigDecimal("20")));

        assertEquals(new BigDecimal("20000.00"), valor);
        assertEquals("QUOTA_LITIS", quotaLitis.modalidade());
    }

    @Test
    @DisplayName("quota litis recusa percentual acima do limite etico de 30% (art. 38 do CED-OAB)")
    void quotaLitisRecusaAcimaDoLimiteEtico() {
        CalculoHonorarioStrategy quotaLitis = new HonorarioQuotaLitis();

        IllegalArgumentException falha = assertThrows(IllegalArgumentException.class,
                () -> quotaLitis.calcular(
                        BaseCalculo.quotaLitis(new BigDecimal("100000"), new BigDecimal("31"))));

        assertTrue(falha.getMessage().contains("limite etico"));
    }

    @Test
    @DisplayName("motor seleciona a estrategia pela modalidade contratada")
    void motorSelecionaEstrategiaPorModalidade() {
        MotorDeHonorarios motor = new MotorDeHonorarios(
                List.of(new HonorarioFixo(), new HonorarioPorHora(), new HonorarioQuotaLitis()));

        assertEquals("FIXO", motor.estrategiaPara("FIXO").modalidade());
        assertEquals("POR_HORA", motor.estrategiaPara("POR_HORA").modalidade());
        assertEquals("QUOTA_LITIS", motor.estrategiaPara("QUOTA_LITIS").modalidade());
    }

    @Test
    @DisplayName("motor recusa modalidade desconhecida")
    void motorRecusaModalidadeDesconhecida() {
        MotorDeHonorarios motor = new MotorDeHonorarios(List.of(new HonorarioFixo()));

        assertThrows(IllegalArgumentException.class, () -> motor.estrategiaPara("PRO_BONO"));
    }

    @Test
    @DisplayName("contrato novo congela o valor calculado pela Strategy da modalidade")
    void contratoCongelaValorCalculado() {
        ContratoHonorario contrato = new ContratoHonorario(
                processo, LocalDate.of(2026, 9, 10),
                BaseCalculo.porHora(new BigDecimal("200"), 8),
                new HonorarioPorHora());

        assertEquals(new BigDecimal("1600"), contrato.getValorContratado());
        assertEquals("POR_HORA", contrato.getModalidade());
        assertEquals(processo, contrato.getNumeroProcesso());
    }

    @Test
    @DisplayName("contrato reconstituido da persistencia nao recalcula o valor")
    void contratoReconstituidoNaoRecalcula() {
        ContratoHonorario reconstituido = new ContratoHonorario(
                1L, processo, "FIXO", BaseCalculo.fixo(new BigDecimal("5000")),
                LocalDate.of(2026, 1, 1), new BigDecimal("999"));

        assertEquals(new BigDecimal("999"), reconstituido.getValorContratado());
    }

    @Test
    @DisplayName("contrato exige processo, data de celebracao, base de calculo e estrategia")
    void contratoExigeCamposObrigatorios() {
        LocalDate hoje = LocalDate.of(2026, 9, 10);
        BaseCalculo base = BaseCalculo.fixo(new BigDecimal("1000"));
        CalculoHonorarioStrategy fixo = new HonorarioFixo();

        assertThrows(IllegalArgumentException.class,
                () -> new ContratoHonorario(null, hoje, base, fixo));
        assertThrows(IllegalArgumentException.class,
                () -> new ContratoHonorario(processo, null, base, fixo));
        assertThrows(IllegalArgumentException.class,
                () -> new ContratoHonorario(processo, hoje, null, fixo));
        assertThrows(IllegalArgumentException.class,
                () -> new ContratoHonorario(processo, hoje, base, null));
    }
}
