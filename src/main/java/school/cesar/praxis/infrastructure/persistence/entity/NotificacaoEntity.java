package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Trilha de auditoria de notificacoes. Em escritorio de advocacia, provar que o
 * responsavel foi avisado do prazo e requisito, nao conveniencia.
 */
@Entity
@Table(name = "notificacao")
public class NotificacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String destinatario;

    @Column(nullable = false, length = 300)
    private String assunto;

    @Column(nullable = false, length = 1000)
    private String corpo;

    @Column(name = "numero_processo", length = 30)
    private String numeroProcesso;

    @Column(nullable = false, length = 20)
    private String canal;

    @Column(name = "registrado_em", nullable = false)
    private LocalDateTime registradoEm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getCorpo() {
        return corpo;
    }

    public void setCorpo(String corpo) {
        this.corpo = corpo;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public void setNumeroProcesso(String numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public LocalDateTime getRegistradoEm() {
        return registradoEm;
    }

    public void setRegistradoEm(LocalDateTime registradoEm) {
        this.registradoEm = registradoEm;
    }
}
