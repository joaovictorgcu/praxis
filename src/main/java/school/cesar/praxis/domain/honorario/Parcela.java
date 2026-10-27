package school.cesar.praxis.domain.honorario;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Uma parcela do contrato de honorarios. Status e encargos sao sempre
 * derivados a partir do "hoje" recebido por parametro (nunca de
 * LocalDate.now()): quem decide que dia e hoje e o Relogio, na borda da
 * aplicacao, para o teste poder controlar o tempo.
 */
public class Parcela {

    static final BigDecimal MULTA_PERCENTUAL = new BigDecimal("0.02");
    static final BigDecimal JUROS_MENSAL_PERCENTUAL = new BigDecimal("0.01");
    private static final BigDecimal DIAS_NO_MES = new BigDecimal("30");
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final int numero;
    private final LocalDate vencimento;
    private final BigDecimal valor;
    private BigDecimal valorPago;
    private LocalDate dataPagamento;

    /** Parcela nova, gerada no parcelamento do contrato: ainda sem pagamento. */
    public Parcela(int numero, LocalDate vencimento, BigDecimal valor) {
        if (numero <= 0) {
            throw new IllegalArgumentException("número da parcela deve ser positivo");
        }
        if (vencimento == null) {
            throw new IllegalArgumentException("vencimento da parcela é obrigatório");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("valor da parcela deve ser positivo");
        }
        this.numero = numero;
        this.vencimento = vencimento;
        this.valor = valor.setScale(2, RoundingMode.HALF_UP);
        this.valorPago = ZERO;
    }

    /** Reconstituicao a partir da persistencia. */
    public Parcela(int numero, LocalDate vencimento, BigDecimal valor,
                   BigDecimal valorPago, LocalDate dataPagamento) {
        this.numero = numero;
        this.vencimento = vencimento;
        this.valor = valor;
        this.valorPago = valorPago;
        this.dataPagamento = dataPagamento;
    }

    public BigDecimal saldo() {
        return valor.subtract(valorPago).setScale(2, RoundingMode.HALF_UP);
    }

    public void registrarPagamento(BigDecimal valorPagamento, LocalDate dataPagamento) {
        if (valorPagamento == null || valorPagamento.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("valor do pagamento deve ser positivo");
        }
        if (dataPagamento == null) {
            throw new IllegalArgumentException("data do pagamento é obrigatória");
        }
        if (valorPagamento.compareTo(saldo()) > 0) {
            throw new IllegalArgumentException("pagamento de " + emReais(valorPagamento)
                    + " maior que o saldo da parcela " + numero + " (" + emReais(saldo()) + ")");
        }
        this.valorPago = valorPago.add(valorPagamento).setScale(2, RoundingMode.HALF_UP);
        this.dataPagamento = dataPagamento;
    }

    /** PAGA tem prioridade sobre atraso: parcela quitada nao fica "em atraso" mesmo se o pagamento veio depois do vencimento. */
    public StatusParcela status(LocalDate hoje) {
        if (saldo().compareTo(BigDecimal.ZERO) <= 0) {
            return StatusParcela.PAGA;
        }
        if (hoje.isAfter(vencimento)) {
            return StatusParcela.EM_ATRASO;
        }
        if (valorPago.compareTo(BigDecimal.ZERO) > 0) {
            return StatusParcela.PARCIALMENTE_PAGA;
        }
        return StatusParcela.PENDENTE;
    }

    public long diasEmAtraso(LocalDate hoje) {
        return status(hoje) == StatusParcela.EM_ATRASO ? ChronoUnit.DAYS.between(vencimento, hoje) : 0;
    }

    /** Multa de 2% sobre o saldo, uma vez so (nao escala com os dias de atraso). */
    public BigDecimal multa(LocalDate hoje) {
        return status(hoje) == StatusParcela.EM_ATRASO
                ? saldo().multiply(MULTA_PERCENTUAL).setScale(2, RoundingMode.HALF_UP)
                : ZERO;
    }

    /** Juros de 1% ao mes, pro rata die (1%/30 por dia de atraso). */
    public BigDecimal juros(LocalDate hoje) {
        long dias = diasEmAtraso(hoje);
        if (dias <= 0) {
            return ZERO;
        }
        BigDecimal taxaDiaria = JUROS_MENSAL_PERCENTUAL.divide(DIAS_NO_MES, 10, RoundingMode.HALF_UP);
        return saldo().multiply(taxaDiaria).multiply(BigDecimal.valueOf(dias)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal valorAtualizado(LocalDate hoje) {
        return saldo().add(multa(hoje)).add(juros(hoje)).setScale(2, RoundingMode.HALF_UP);
    }

    /** Formata em reais (pt-BR) para mensagem de erro, legível por quem não é dev. */
    private static String emReais(BigDecimal valor) {
        return String.format(new Locale("pt", "BR"), "R$ %,.2f", valor);
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }
}
