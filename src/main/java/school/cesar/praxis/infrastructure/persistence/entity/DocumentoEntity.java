package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;
import school.cesar.praxis.domain.documento.TipoDocumento;

import java.time.LocalDate;

/** Mapeamento objeto-relacional do agregado DocumentoGerado. */
@Entity
@Table(name = "documento", indexes = {
        @Index(name = "idx_documento_processo", columnList = "numero_processo")
})
public class DocumentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_processo", nullable = false, length = 30)
    private String numeroProcesso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoDocumento tipo;

    @Lob
    @Column(nullable = false)
    private String conteudo;

    @Column(name = "gerado_em", nullable = false)
    private LocalDate geradoEm;

    @Column(name = "gerado_por_oab", length = 20)
    private String geradoPorOab;

    @Column(name = "segredo_justica", nullable = false)
    private boolean segredoJustica;

    /** OABs habilitadas nos autos, separadas por virgula (consultadas pelo Proxy). */
    @Column(name = "oabs_habilitadas", length = 500)
    private String oabsHabilitadas;

    @Column(nullable = false, length = 20)
    private String status = "RASCUNHO";

    @Lob
    @Column(name = "historico")
    private String historico;

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

    public TipoDocumento getTipo() {
        return tipo;
    }

    public void setTipo(TipoDocumento tipo) {
        this.tipo = tipo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public LocalDate getGeradoEm() {
        return geradoEm;
    }

    public void setGeradoEm(LocalDate geradoEm) {
        this.geradoEm = geradoEm;
    }

    public String getGeradoPorOab() {
        return geradoPorOab;
    }

    public void setGeradoPorOab(String geradoPorOab) {
        this.geradoPorOab = geradoPorOab;
    }

    public boolean isSegredoJustica() {
        return segredoJustica;
    }

    public void setSegredoJustica(boolean segredoJustica) {
        this.segredoJustica = segredoJustica;
    }

    public String getOabsHabilitadas() {
        return oabsHabilitadas;
    }

    public void setOabsHabilitadas(String oabsHabilitadas) {
        this.oabsHabilitadas = oabsHabilitadas;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHistorico() {
        return historico;
    }

    public void setHistorico(String historico) {
        this.historico = historico;
    }
}