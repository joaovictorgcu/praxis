package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;
import school.cesar.praxis.domain.advogado.StatusAdvogado;

import java.time.LocalDate;

@Entity
@Table(name = "advogado",
        uniqueConstraints = @UniqueConstraint(name = "uk_advogado_oab", columnNames = "oab"))
public class AdvogadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false, length = 20)
    private String oab;

    @Column(length = 20)
    private String telefone;

    @Column(length = 80)
    private String especialidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAdvogado status;

    @Column(nullable = false)
    private boolean disponivel;

    @Column(name = "data_admissao", nullable = false)
    private LocalDate dataAdmissao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOab() {
        return oab;
    }

    public void setOab(String oab) {
        this.oab = oab;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public StatusAdvogado getStatus() {
        return status;
    }

    public void setStatus(StatusAdvogado status) {
        this.status = status;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    public LocalDate getDataAdmissao() {
        return dataAdmissao;
    }

    public void setDataAdmissao(LocalDate dataAdmissao) {
        this.dataAdmissao = dataAdmissao;
    }
}
