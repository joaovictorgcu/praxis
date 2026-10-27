package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contrato_honorario", indexes = @Index(name = "idx_contrato_processo", columnList = "numero_processo"))
public class ContratoHonorarioEntity {

    static final int TEXTO_LONGO = 1_000_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_processo", nullable = false, length = 25)
    private String numeroProcesso;

    @Column(nullable = false, length = 20)
    private String modalidade;

    @Column(name = "celebrado_em", nullable = false)
    private LocalDate celebradoEm;

    @Column(name = "valor_fixo", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorFixo;

    @Column(name = "valor_hora", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorHora;

    @Column(name = "horas_trabalhadas", nullable = false)
    private int horasTrabalhadas;

    @Column(name = "valor_causa", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorCausa;

    @Column(name = "percentual_exito", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualExito;

    @Column(name = "valor_contratado", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorContratado;

    @Column(name = "quantidade_parcelas", nullable = false)
    private int quantidadeParcelas;

    @Column(name = "primeiro_vencimento")
    private LocalDate primeiroVencimento;

    @Column(name = "valor_obtido", precision = 15, scale = 2)
    private BigDecimal valorObtido;

    @Column(name = "honorarios_sucumbencia", precision = 15, scale = 2)
    private BigDecimal honorariosSucumbencia;

    @Column(name = "historico", length = TEXTO_LONGO)
    private String historico;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "contrato_id")
    private List<ParcelaEntity> parcelas = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public void setNumeroProcesso(String numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
    }

    public String getModalidade() {
        return modalidade;
    }

    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }

    public LocalDate getCelebradoEm() {
        return celebradoEm;
    }

    public void setCelebradoEm(LocalDate celebradoEm) {
        this.celebradoEm = celebradoEm;
    }

    public BigDecimal getValorFixo() {
        return valorFixo;
    }

    public void setValorFixo(BigDecimal valorFixo) {
        this.valorFixo = valorFixo;
    }

    public BigDecimal getValorHora() {
        return valorHora;
    }

    public void setValorHora(BigDecimal valorHora) {
        this.valorHora = valorHora;
    }

    public int getHorasTrabalhadas() {
        return horasTrabalhadas;
    }

    public void setHorasTrabalhadas(int horasTrabalhadas) {
        this.horasTrabalhadas = horasTrabalhadas;
    }

    public BigDecimal getValorCausa() {
        return valorCausa;
    }

    public void setValorCausa(BigDecimal valorCausa) {
        this.valorCausa = valorCausa;
    }

    public BigDecimal getPercentualExito() {
        return percentualExito;
    }

    public void setPercentualExito(BigDecimal percentualExito) {
        this.percentualExito = percentualExito;
    }

    public BigDecimal getValorContratado() {
        return valorContratado;
    }

    public void setValorContratado(BigDecimal valorContratado) {
        this.valorContratado = valorContratado;
    }

    public int getQuantidadeParcelas() {
        return quantidadeParcelas;
    }

    public void setQuantidadeParcelas(int quantidadeParcelas) {
        this.quantidadeParcelas = quantidadeParcelas;
    }

    public LocalDate getPrimeiroVencimento() {
        return primeiroVencimento;
    }

    public void setPrimeiroVencimento(LocalDate primeiroVencimento) {
        this.primeiroVencimento = primeiroVencimento;
    }

    public BigDecimal getValorObtido() {
        return valorObtido;
    }

    public void setValorObtido(BigDecimal valorObtido) {
        this.valorObtido = valorObtido;
    }

    public BigDecimal getHonorariosSucumbencia() {
        return honorariosSucumbencia;
    }

    public void setHonorariosSucumbencia(BigDecimal honorariosSucumbencia) {
        this.honorariosSucumbencia = honorariosSucumbencia;
    }

    public String getHistorico() {
        return historico;
    }

    public void setHistorico(String historico) {
        this.historico = historico;
    }

    public List<ParcelaEntity> getParcelas() {
        return parcelas;
    }

    public void setParcelas(List<ParcelaEntity> parcelas) {
        this.parcelas = parcelas;
    }
}
