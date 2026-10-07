package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "comarca",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_comarca_local_nome",
        columnNames = { "uf", "municipio", "nome" }
    )
)
public class ComarcaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 160)
    private String nome;

    @Column(name = "municipio", nullable = false, length = 120)
    private String municipio;

    @Column(name = "uf", nullable = false, length = 2)
    private String uf;

    @Column(name = "tribunal", nullable = false, length = 120)
    private String tribunal;

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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getTribunal() {
        return tribunal;
    }

    public void setTribunal(String tribunal) {
        this.tribunal = tribunal;
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
