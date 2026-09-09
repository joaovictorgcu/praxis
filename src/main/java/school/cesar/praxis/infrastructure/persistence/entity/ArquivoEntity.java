package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;
import school.cesar.praxis.domain.anexo.TipoArquivo;

import java.time.LocalDate;

/** Mapeamento objeto-relacional do agregado ArquivoAnexo. */
@Entity
@Table(name = "arquivo_anexo",
        indexes = @Index(name = "idx_anexo_processo", columnList = "numero_processo"))
public class ArquivoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_processo", nullable = false, length = 25)
    private String numeroProcesso;

    @Column(nullable = false, length = 220)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoArquivo tipo;

    /** Conteudo em BLOB: a escolha foi guardar o binario no banco. */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private byte[] conteudo;

    @Column(length = 300)
    private String descricao;

    @Column(name = "anexado_em", nullable = false)
    private LocalDate anexadoEm;

    @Column(name = "anexado_por_oab", length = 20)
    private String anexadoPorOab;

    @Column(name = "segredo_justica", nullable = false)
    private boolean segredoJustica;

    /** OABs habilitadas, separadas por virgula (mesma convencao do documento). */
    @Column(name = "oabs_habilitadas", length = 500)
    private String oabsHabilitadas;

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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoArquivo getTipo() {
        return tipo;
    }

    public void setTipo(TipoArquivo tipo) {
        this.tipo = tipo;
    }

    public byte[] getConteudo() {
        return conteudo;
    }

    public void setConteudo(byte[] conteudo) {
        this.conteudo = conteudo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getAnexadoEm() {
        return anexadoEm;
    }

    public void setAnexadoEm(LocalDate anexadoEm) {
        this.anexadoEm = anexadoEm;
    }

    public String getAnexadoPorOab() {
        return anexadoPorOab;
    }

    public void setAnexadoPorOab(String anexadoPorOab) {
        this.anexadoPorOab = anexadoPorOab;
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
}
