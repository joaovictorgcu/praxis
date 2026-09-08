package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/** Mapeamento objeto-relacional do agregado Processo. */
@Entity
@Table(name = "processo")
public class ProcessoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cnj", nullable = false, unique = true, length = 30)
    private String numeroCnj;

    @Column(nullable = false, length = 200)
    private String cliente;

    @Column(nullable = false, length = 120)
    private String comarca;

    @Column(name = "segredo_justica", nullable = false)
    private boolean segredoJustica;

    @Column(name = "responsavel_nome", nullable = false, length = 120)
    private String responsavelNome;

    @Column(name = "responsavel_email", nullable = false, length = 160)
    private String responsavelEmail;

    @Column(name = "responsavel_oab", nullable = false, length = 20)
    private String responsavelOab;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "processo_id")
    private List<AndamentoEntity> andamentos = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroCnj() {
        return numeroCnj;
    }

    public void setNumeroCnj(String numeroCnj) {
        this.numeroCnj = numeroCnj;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getComarca() {
        return comarca;
    }

    public void setComarca(String comarca) {
        this.comarca = comarca;
    }

    public boolean isSegredoJustica() {
        return segredoJustica;
    }

    public void setSegredoJustica(boolean segredoJustica) {
        this.segredoJustica = segredoJustica;
    }

    public String getResponsavelNome() {
        return responsavelNome;
    }

    public void setResponsavelNome(String responsavelNome) {
        this.responsavelNome = responsavelNome;
    }

    public String getResponsavelEmail() {
        return responsavelEmail;
    }

    public void setResponsavelEmail(String responsavelEmail) {
        this.responsavelEmail = responsavelEmail;
    }

    public String getResponsavelOab() {
        return responsavelOab;
    }

    public void setResponsavelOab(String responsavelOab) {
        this.responsavelOab = responsavelOab;
    }

    public List<AndamentoEntity> getAndamentos() {
        return andamentos;
    }

    public void setAndamentos(List<AndamentoEntity> andamentos) {
        this.andamentos = andamentos;
    }
}
