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

    /** Acima de 10 485 760 o dialeto PostgreSQL vira text; abaixo, varchar(n). */
    static final int TEXTO_LONGO = 1_000_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_processo", nullable = false, length = 30)
    private String numeroProcesso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoDocumento tipo;

    /**
     * Texto longo como VARCHAR largo (text no PostgreSQL, character varying no
     * H2), e nao LOB: o CLOB do PostgreSQL e um large object (oid) fora da tabela.
     */
    @Column(nullable = false, length = TEXTO_LONGO)
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

    @Column(name = "historico", length = TEXTO_LONGO)
    private String historico;

    /** Modelo cadastrado usado na geracao; nulo quando foi a peca compilada. */
    @Column(name = "modelo_codigo", length = 40)
    private String modeloCodigo;

    /** Campos da geracao, "chave=valor" percent-encoded e separados por '&'. */
    @Column(name = "campos_geracao", length = TEXTO_LONGO)
    private String camposGeracao;

    /** Numero da versao de {@code documento_versao} que esta em {@code conteudo}. */
    @Column(name = "versao_atual", nullable = false)
    private int versaoAtual = 1;

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

    public String getModeloCodigo() {
        return modeloCodigo;
    }

    public void setModeloCodigo(String modeloCodigo) {
        this.modeloCodigo = modeloCodigo;
    }

    public String getCamposGeracao() {
        return camposGeracao;
    }

    public void setCamposGeracao(String camposGeracao) {
        this.camposGeracao = camposGeracao;
    }

    public int getVersaoAtual() {
        return versaoAtual;
    }

    public void setVersaoAtual(int versaoAtual) {
        this.versaoAtual = versaoAtual;
    }
}
