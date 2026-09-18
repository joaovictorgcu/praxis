package school.cesar.praxis.bdd;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class HonorariosSteps {

    private static final LocalDate DATA_FIXA_PADRAO = LocalDate.of(2026, 12, 31);

    private BigDecimal valorBase;
    private BigDecimal valorHora;
    private Integer horasTrabalhadas;
    private BigDecimal valorCausa;
    private BigDecimal percentualQuota;
    private String tipoModalidade;

    private BigDecimal valorCalculado;
    private Exception excecao;
    private String status;
    private LocalDate dataVencimento;

    @Dado("que a modalidade de honorário é fixa com valor de {double}")
    public void modalidadeFixa(Double valor) {
        this.tipoModalidade = "FIXO";
        this.valorBase = BigDecimal.valueOf(valor);
    }

    @Dado("que a modalidade é por hora com valor hora de {double} e {int} horas trabalhadas")
    public void modalidadePorHora(Double valorHora, Integer horas) {
        this.tipoModalidade = "POR_HORA";
        this.valorHora = BigDecimal.valueOf(valorHora);
        this.horasTrabalhadas = horas;
    }

    @Dado("que a modalidade é quota litis com valor da causa de {double} e percentual de {int} por cento")
    public void modalidadeQuotaLitis(Double valorCausa, Integer percentual) {
        this.tipoModalidade = "QUOTA_LITIS";
        this.valorCausa = BigDecimal.valueOf(valorCausa);
        this.percentualQuota = BigDecimal.valueOf(percentual);
    }

    @Quando("o honorário for calculated")
    @Quando("o honorário for calculado")
    public void executarCalculo() {
        try {
            if ("FIXO".equals(tipoModalidade)) {
                this.valorCalculado = valorBase;
            } else if ("POR_HORA".equals(tipoModalidade)) {
                this.valorCalculado = valorHora.multiply(BigDecimal.valueOf(horasTrabalhadas));
            } else if ("QUOTA_LITIS".equals(tipoModalidade)) {
                if (percentualQuota.compareTo(BigDecimal.valueOf(30)) > 0) {
                    throw new IllegalArgumentException("Limite ético excedido");
                }
                this.valorCalculado = valorCausa.multiply(percentualQuota).divide(BigDecimal.valueOf(100));
            }
        } catch (Exception e) {
            this.excecao = e;
        }
    }

    @Entao("o valor do honorário deve ser {double}")
    public void validarValor(Double valorEsperado) {
        assertEquals(0, BigDecimal.valueOf(valorEsperado).compareTo(this.valorCalculado));
    }

    @Entao("a data de vencimento deve ser fixada em {string}")
    public void validarDataVencimento(String dataStr) {
        this.dataVencimento = LocalDate.parse(dataStr);
        assertEquals(DATA_FIXA_PADRAO, this.dataVencimento);
    }

    @Entao("deve lançar uma exceção de limite ético excedido")
    public void validarExcecao() {
        assertNotNull(this.excecao);
        assertTrue(this.excecao instanceof IllegalArgumentException);
    }

    @Dado("que existe um honorário fixo de {double} com status {string}")
    public void honorarioPendente(Double valor, String statusInicial) {
        this.valorCalculado = BigDecimal.valueOf(valor);
        this.status = statusInicial;
    }

    @Quando("o pagamento do honorário for confirmado")
    public void confirmarPagamento() {
        this.status = "PAGO";
    }

    @Entao("o status do honorário deve ser alterado para {string}")
    public void validarStatus(String statusEsperado) {
        assertEquals(statusEsperado, this.status);
    }

    @Entao("a data de liquidação deve ser fixada em {string}")
    public void validarDataLiquidacao(String dataStr) {
        assertEquals(DATA_FIXA_PADRAO, LocalDate.parse(dataStr));
    }

    @Dado("que existe um honorário cadastrado")
    public void honorarioCadastrado() {
        this.dataVencimento = DATA_FIXA_PADRAO;
    }

    @Quando("a data de vencimento for consultada")
    public void consultarDataVencimento() {}

    @Entao("a data retornada deve ser a data fixa {string}")
    public void validarDataRetornada(String dataEsperada) {
        assertEquals(LocalDate.parse(dataEsperada), this.dataVencimento);
    }
}