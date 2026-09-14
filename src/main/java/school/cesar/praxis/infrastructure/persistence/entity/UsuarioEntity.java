package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;
import school.cesar.praxis.domain.usuario.Papel;

/** Mapeamento objeto-relacional do agregado Usuario. */
@Entity
@Table(name = "usuario", uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_usuario_oab", columnNames = "oab")
})
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false, length = 20)
    private String oab;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Papel papel;

    @Column(name = "senha_codificada", nullable = false, length = 300)
    private String senhaCodificada;

    @Column(name = "senha_provisoria", nullable = false)
    private boolean senhaProvisoria;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getOab() { return oab; }
    public void setOab(String oab) { this.oab = oab; }
    public Papel getPapel() { return papel; }
    public void setPapel(Papel papel) { this.papel = papel; }
    public String getSenhaCodificada() { return senhaCodificada; }
    public void setSenhaCodificada(String senhaCodificada) { this.senhaCodificada = senhaCodificada; }
    public boolean isSenhaProvisoria() { return senhaProvisoria; }
    public void setSenhaProvisoria(boolean senhaProvisoria) { this.senhaProvisoria = senhaProvisoria; }
}
