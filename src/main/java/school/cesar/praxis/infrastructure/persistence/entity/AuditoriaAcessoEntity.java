package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_acesso", indexes = {
        @Index(name = "idx_auditoria_processo", columnList = "numero_processo"),
        @Index(name = "idx_auditoria_oab", columnList = "oab")
})
public class AuditoriaAcessoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_evento", nullable = false, length = 30)
    private String tipoEvento;

    @Column(name = "conteudo_tipo", length = 20)
    private String conteudoTipo;

    @Column(name = "conteudo_id")
    private Long conteudoId;

    @Column(name = "numero_processo", nullable = false, length = 25)
    private String numeroProcesso;

    @Column(length = 20)
    private String oab;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_nome", length = 150)
    private String usuarioNome;

    @Column(nullable = false)
    private LocalDateTime quando;

    @Column(nullable = false, length = 10)
    private String resultado;

    @Column(length = 500)
    private String motivo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getConteudoTipo() {
        return conteudoTipo;
    }

    public void setConteudoTipo(String conteudoTipo) {
        this.conteudoTipo = conteudoTipo;
    }

    public Long getConteudoId() {
        return conteudoId;
    }

    public void setConteudoId(Long conteudoId) {
        this.conteudoId = conteudoId;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public void setNumeroProcesso(String numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
    }

    public String getOab() {
        return oab;
    }

    public void setOab(String oab) {
        this.oab = oab;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public void setUsuarioNome(String usuarioNome) {
        this.usuarioNome = usuarioNome;
    }

    public LocalDateTime getQuando() {
        return quando;
    }

    public void setQuando(LocalDateTime quando) {
        this.quando = quando;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
