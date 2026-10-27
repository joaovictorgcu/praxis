package school.cesar.praxis.domain.honorario;

import school.cesar.praxis.domain.processo.NumeroCnj;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Raiz de agregado do subdominio de apoio <b>Honorarios</b>: contrato de
 * honorarios celebrado com o cliente para um processo, com parcelamento e
 * controle financeiro das parcelas.
 *
 * <p>Para quota litis, o valor contratado nasce como estimativa (sobre o valor
 * da causa informado na contratacao) e so vira valor final - com as parcelas
 * geradas - quando o resultado da causa e registrado, sobre o proveito
 * economico de verdade. As demais modalidades parcelam o valor final direto na
 * contratacao.
 */
public class ContratoHonorario {

    private final Long id;
    private final NumeroCnj numeroProcesso;
    private final String modalidade;
    private final BaseCalculo baseCalculo;
    private final LocalDate celebradoEm;
    private final int quantidadeParcelas;
    private final LocalDate primeiroVencimento;
    private BigDecimal valorContratado;
    private BigDecimal valorObtido;
    private BigDecimal honorariosSucumbencia;
    private List<Parcela> parcelas;
    private final List<RegistroHonorario> historico;

    /** Construcao de um contrato novo: o valor e derivado da Strategy. */
    public ContratoHonorario(NumeroCnj numeroProcesso,
                             LocalDate celebradoEm,
                             BaseCalculo baseCalculo,
                             CalculoHonorarioStrategy calculo,
                             int quantidadeParcelas,
                             LocalDate primeiroVencimento) {
        if (numeroProcesso == null) {
            throw new IllegalArgumentException("contrato de honorário exige processo");
        }
        if (celebradoEm == null) {
            throw new IllegalArgumentException("data de celebração é obrigatória");
        }
        if (baseCalculo == null) {
            throw new IllegalArgumentException("base de cálculo é obrigatória");
        }
        if (calculo == null) {
            throw new IllegalArgumentException("estratégia de cálculo é obrigatória");
        }
        if (quantidadeParcelas < 1 || quantidadeParcelas > 24) {
            throw new IllegalArgumentException("número de parcelas deve estar entre 1 e 24");
        }
        if (primeiroVencimento == null) {
            throw new IllegalArgumentException("data do primeiro vencimento é obrigatória");
        }
        this.id = null;
        this.numeroProcesso = numeroProcesso;
        this.celebradoEm = celebradoEm;
        this.baseCalculo = baseCalculo;
        this.modalidade = calculo.modalidade();
        this.valorContratado = calculo.calcular(baseCalculo);
        this.quantidadeParcelas = quantidadeParcelas;
        this.primeiroVencimento = primeiroVencimento;
        this.parcelas = new ArrayList<>();
        this.historico = new ArrayList<>();

        boolean aguardaResultado = "QUOTA_LITIS".equals(modalidade);
        registrarHistorico("CONTRATACAO", "Contrato celebrado na modalidade " + modalidade
                + ", valor " + (aguardaResultado ? "estimado " : "") + emReais(valorContratado)
                + " em " + quantidadeParcelas + " parcela(s)");
        if (!aguardaResultado) {
            this.parcelas = gerarParcelas(valorContratado);
        }
    }

    /** Reconstituicao a partir da persistencia (nao recalcula nada). */
    public ContratoHonorario(Long id,
                             NumeroCnj numeroProcesso,
                             String modalidade,
                             BaseCalculo baseCalculo,
                             LocalDate celebradoEm,
                             BigDecimal valorContratado,
                             int quantidadeParcelas,
                             LocalDate primeiroVencimento,
                             List<Parcela> parcelas,
                             BigDecimal valorObtido,
                             BigDecimal honorariosSucumbencia,
                             List<RegistroHonorario> historico) {
        this.id = id;
        this.numeroProcesso = numeroProcesso;
        this.modalidade = modalidade;
        this.baseCalculo = baseCalculo;
        this.celebradoEm = celebradoEm;
        this.valorContratado = valorContratado;
        this.quantidadeParcelas = quantidadeParcelas;
        this.primeiroVencimento = primeiroVencimento;
        this.parcelas = new ArrayList<>(parcelas);
        this.valorObtido = valorObtido;
        this.honorariosSucumbencia = honorariosSucumbencia;
        this.historico = new ArrayList<>(historico);
    }

    /** So a quota litis nasce assim, e so ate o resultado da causa ser registrado. */
    public boolean aguardandoResultado() {
        return "QUOTA_LITIS".equals(modalidade) && valorObtido == null;
    }

    /**
     * Registra o proveito economico real e os honorarios de sucumbencia.
     * Reaplica a mesma Strategy do contrato, agora sobre o valor obtido de
     * verdade - o calculo continua sendo so dela, nao vira if/switch aqui.
     * Causa sem exito (valor obtido zero) encerra o contrato sem cobranca.
     */
    public void registrarResultado(BigDecimal valorObtido, BigDecimal honorariosSucumbencia,
                                   CalculoHonorarioStrategy calculo) {
        if (!"QUOTA_LITIS".equals(modalidade)) {
            throw new IllegalStateException("resultado da causa so se aplica a contrato de quota litis");
        }
        if (!aguardandoResultado()) {
            throw new IllegalStateException("resultado da causa ja foi registrado para este contrato");
        }
        if (valorObtido == null || valorObtido.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("valor obtido pelo cliente não pode ser negativo");
        }
        BigDecimal sucumbencia = (honorariosSucumbencia == null ? BigDecimal.ZERO : honorariosSucumbencia)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal obtido = valorObtido.setScale(2, RoundingMode.HALF_UP);

        if (obtido.compareTo(BigDecimal.ZERO) == 0) {
            this.valorObtido = obtido;
            this.honorariosSucumbencia = sucumbencia;
            this.valorContratado = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            this.parcelas = new ArrayList<>();
            registrarHistorico("RESULTADO", "Causa sem êxito: contrato encerrado sem cobrança");
            return;
        }

        BaseCalculo baseFinal = new BaseCalculo(BigDecimal.ZERO, BigDecimal.ZERO, 0,
                obtido, baseCalculo.percentualExito());
        BigDecimal honorariosContratuais = calculo.calcular(baseFinal);

        BigDecimal totalHonorarios = honorariosContratuais.add(sucumbencia);
        if (totalHonorarios.compareTo(obtido) > 0) {
            throw new IllegalArgumentException("honorários contratuais (" + emReais(honorariosContratuais)
                    + ") mais honorários de sucumbência (" + emReais(sucumbencia)
                    + ") superam o valor obtido pelo cliente (" + emReais(obtido)
                    + ") - art. 50 do Código de Ética da OAB");
        }

        this.valorObtido = obtido;
        this.honorariosSucumbencia = sucumbencia;
        this.valorContratado = honorariosContratuais;
        this.parcelas = gerarParcelas(honorariosContratuais);
        registrarHistorico("RESULTADO", "Resultado registrado: valor obtido " + emReais(obtido)
                + ", honorários contratuais " + emReais(honorariosContratuais)
                + ", sucumbência " + emReais(sucumbencia));
    }

    /** Divide o valor em parcelas iguais; a diferenca de centavos vai para a ultima, pra soma bater exato. */
    private List<Parcela> gerarParcelas(BigDecimal valor) {
        BigDecimal valorParcela = valor.divide(BigDecimal.valueOf(quantidadeParcelas), 2, RoundingMode.HALF_UP);
        BigDecimal somado = BigDecimal.ZERO;
        List<Parcela> novas = new ArrayList<>();
        for (int numero = 1; numero <= quantidadeParcelas; numero++) {
            LocalDate vencimento = primeiroVencimento.plusMonths(numero - 1L);
            BigDecimal valorDaParcela = numero < quantidadeParcelas ? valorParcela : valor.subtract(somado);
            somado = somado.add(valorDaParcela);
            novas.add(new Parcela(numero, vencimento, valorDaParcela));
        }
        return novas;
    }

    public void registrarPagamento(int numeroParcela, BigDecimal valor, LocalDate dataPagamento) {
        Parcela parcela = parcelas.stream()
                .filter(p -> p.getNumero() == numeroParcela)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "parcela " + numeroParcela + " não encontrada no contrato"));
        parcela.registrarPagamento(valor, dataPagamento);
        registrarHistorico("PAGAMENTO", "Parcela " + numeroParcela + ": pagamento de " + emReais(valor)
                + " em " + DATA_BR.format(dataPagamento));
    }

    /** Status do contrato, derivado das parcelas: atraso pesa mais que pagamento parcial. */
    public StatusContrato status(LocalDate hoje) {
        if (parcelas.isEmpty()) {
            return aguardandoResultado() ? StatusContrato.EM_ABERTO : StatusContrato.QUITADO;
        }
        if (parcelas.stream().anyMatch(p -> p.status(hoje) == StatusParcela.EM_ATRASO)) {
            return StatusContrato.EM_ATRASO;
        }
        if (parcelas.stream().allMatch(p -> p.status(hoje) == StatusParcela.PAGA)) {
            return StatusContrato.QUITADO;
        }
        boolean algumPagamento = parcelas.stream().anyMatch(p -> p.getValorPago().compareTo(BigDecimal.ZERO) > 0);
        return algumPagamento ? StatusContrato.PARCIALMENTE_PAGO : StatusContrato.EM_ABERTO;
    }

    public Optional<Parcela> proximaParcelaEmAberto(LocalDate hoje) {
        return parcelas.stream()
                .filter(p -> p.status(hoje) != StatusParcela.PAGA)
                .min((a, b) -> a.getVencimento().compareTo(b.getVencimento()));
    }

    public BigDecimal totalPago() {
        return parcelas.stream().map(Parcela::getValorPago).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal saldoDevedor() {
        return parcelas.stream().map(Parcela::saldo).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void registrarHistorico(String tipo, String descricao) {
        historico.add(new RegistroHonorario(tipo, descricao, LocalDateTime.now()));
    }

    /** Formata em reais (pt-BR) para mensagem de erro e histórico, legível por quem não é dev. */
    static String emReais(BigDecimal valor) {
        return String.format(new Locale("pt", "BR"), "R$ %,.2f", valor);
    }

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Long getId() {
        return id;
    }

    public NumeroCnj getNumeroProcesso() {
        return numeroProcesso;
    }

    public String getModalidade() {
        return modalidade;
    }

    public BaseCalculo getBaseCalculo() {
        return baseCalculo;
    }

    public LocalDate getCelebradoEm() {
        return celebradoEm;
    }

    public BigDecimal getValorContratado() {
        return valorContratado;
    }

    public int getQuantidadeParcelas() {
        return quantidadeParcelas;
    }

    public LocalDate getPrimeiroVencimento() {
        return primeiroVencimento;
    }

    public List<Parcela> getParcelas() {
        return List.copyOf(parcelas);
    }

    public BigDecimal getValorObtido() {
        return valorObtido;
    }

    public BigDecimal getHonorariosSucumbencia() {
        return honorariosSucumbencia;
    }

    public List<RegistroHonorario> getHistorico() {
        return List.copyOf(historico);
    }
}
