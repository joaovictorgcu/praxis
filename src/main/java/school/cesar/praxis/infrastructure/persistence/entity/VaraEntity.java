package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "vara_judicial",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_vara_judicial_comarca_nome",
        columnNames = { "comarca_id", "nome" }
    ),
    indexes = @Index(name = "idx_vara_judicial_comarca", columnList = "comarca_id")
)
public class VaraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comarca_id", nullable = false)
    private Long comarcaId;

    @Column(name = "nome", nullable = false, length = 160)
    private String nome;

    @Column(name = "competencia", nullable = false, length = 120)
    private String competencia;

    @Column(name = "endereco", length = 300)
    private String endereco;

    @Column(name = "telefone", length = 30)
    private String telefone;

    @Column(name = "email", length = 160)
    private String email;

    @Column(name = "horario_atendimento", length = 120)
    private String horarioAtendimento;

    @Column(name = "observacoes", length = 1000)
    private String observacoes;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDate dataCadastro;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getComarcaId() {
        return comarcaId;
    }

    public void setComarcaId(Long comarcaId) {
        this.comarcaId = comarcaId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCompetencia() {
        return competencia;
    }

    public void setCompetencia(String competencia) {
        this.competencia = competencia;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHorarioAtendimento() {
        return horarioAtendimento;
    }

    public void setHorarioAtendimento(String horarioAtendimento) {
        this.horarioAtendimento = horarioAtendimento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
