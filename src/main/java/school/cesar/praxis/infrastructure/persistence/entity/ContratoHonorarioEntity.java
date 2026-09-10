package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contrato_honorario", indexes = @Index(name = "idx_contrato_processo", columnList = "numero_processo"))
public class ContratoHonorarioEntity {

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
}
