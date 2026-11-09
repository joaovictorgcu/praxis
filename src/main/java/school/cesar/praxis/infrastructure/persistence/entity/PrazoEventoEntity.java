package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;
import school.cesar.praxis.domain.prazo.TipoEventoPrazo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Linha da trilha do prazo. Referencia o prazo pelo id (chave estrangeira na
 * migracao), sem associacao JPA: a trilha e lida a parte, so na ficha do prazo.
 */
@Entity
@Table(name = "prazo_evento", indexes = {
        @Index(name = "idx_prazo_evento_prazo", columnList = "prazo_id, ocorrido_em")
})
public class PrazoEventoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prazo_id", nullable = false)
    private Long prazoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEventoPrazo tipo;

    @Column(name = "ocorrido_em", nullable = false)
    private LocalDateTime ocorridoEm;

    @Column(name = "vencimento_anterior")
    private LocalDate vencimentoAnterior;

    @Column(name = "vencimento_novo")
    private LocalDate vencimentoNovo;

    @Column(length = 500)
    private String detalhe;

    @Column(name = "autor_oab", length = 20)
    private String autorOab;

    @Column(name = "autor_nome", length = 150)
    private String autorNome;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPrazoId() {
        return prazoId;
    }

    public void setPrazoId(Long prazoId) {
        this.prazoId = prazoId;
    }

    public TipoEventoPrazo getTipo() {
        return tipo;
    }

    public void setTipo(TipoEventoPrazo tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getOcorridoEm() {
        return ocorridoEm;
    }

    public void setOcorridoEm(LocalDateTime ocorridoEm) {
        this.ocorridoEm = ocorridoEm;
    }

    public LocalDate getVencimentoAnterior() {
        return vencimentoAnterior;
    }

    public void setVencimentoAnterior(LocalDate vencimentoAnterior) {
        this.vencimentoAnterior = vencimentoAnterior;
    }

    public LocalDate getVencimentoNovo() {
        return vencimentoNovo;
    }

    public void setVencimentoNovo(LocalDate vencimentoNovo) {
        this.vencimentoNovo = vencimentoNovo;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public void setDetalhe(String detalhe) {
        this.detalhe = detalhe;
    }

    public String getAutorOab() {
        return autorOab;
    }

    public void setAutorOab(String autorOab) {
        this.autorOab = autorOab;
    }

    public String getAutorNome() {
        return autorNome;
    }

    public void setAutorNome(String autorNome) {
        this.autorNome = autorNome;
    }
}
