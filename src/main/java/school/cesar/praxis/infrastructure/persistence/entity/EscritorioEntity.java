package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;

/** Mapeamento objeto-relacional do agregado Escritorio. */
@Entity
@Table(name = "escritorio", uniqueConstraints = {
        @UniqueConstraint(name = "uk_escritorio_email", columnNames = "email")
})
public class EscritorioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(length = 20)
    private String cnpj;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 2)
    private String uf;

    @Column(length = 120)
    private String comarca;

    @Column(name = "senha_codificada", nullable = false, length = 300)
    private String senhaCodificada;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    public String getComarca() { return comarca; }
    public void setComarca(String comarca) { this.comarca = comarca; }
    public String getSenhaCodificada() { return senhaCodificada; }
    public void setSenhaCodificada(String senhaCodificada) { this.senhaCodificada = senhaCodificada; }
}
