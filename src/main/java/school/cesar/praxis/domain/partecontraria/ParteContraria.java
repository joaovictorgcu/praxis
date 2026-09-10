package school.cesar.praxis.domain.partecontraria;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade de domínio que representa uma parte contrária em um processo.
 * Responsabilidades:
 * - Manter os dados da parte contrária
 * - Validar regras de negócio
 */
@Entity
@Table(name = "partes_contrarias")
public class ParteContraria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String cpfOuCnpj;

    @Column(name = "tipo_pessoa", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoPessoa tipoPessoa;

    @Column
    private String email;

    @Column
    private String telefone;

    @Column
    private String endereco;

    @Column
    private String cidade;

    @Column
    private String estado;

    @Column
    private String cep;

    @Column
    private String observacoes;

    @Column(nullable = false)
    private boolean ativa = true;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @Column(name = "atualizada_em")
    private LocalDateTime atualizadaEm;

    // Construtores

    public ParteContraria() {
        this.criadaEm = LocalDateTime.now();
    }

    public ParteContraria(String nome, String cpfOuCnpj, TipoPessoa tipoPessoa) {
        validarParametros(nome, cpfOuCnpj, tipoPessoa);
        this.nome = nome;
        this.cpfOuCnpj = cpfOuCnpj;
        this.tipoPessoa = tipoPessoa;
        this.criadaEm = LocalDateTime.now();
        this.ativa = true;
    }

    // Métodos de negócio

    /**
     * Desativa a parte contrária sem removê-la do banco
     */
    public void desativar() {
        this.ativa = false;
        this.atualizadaEm = LocalDateTime.now();
    }

    /**
     * Reativa a parte contrária
     */
    public void reativar() {
        this.ativa = true;
        this.atualizadaEm = LocalDateTime.now();
    }

    /**
     * Atualiza os dados da parte contrária
     */
    public void atualizar(String nome, String email, String telefone, 
                         String endereco, String cidade, String estado, 
                         String cep, String observacoes) {
        if (nome != null && !nome.isBlank()) {
            this.nome = nome;
        }
        this.email = email;
        this.telefone = telefone;
        this.endereco = endereco;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
        this.observacoes = observacoes;
        this.atualizadaEm = LocalDateTime.now();
    }

    // Validações

    private void validarParametros(String nome, String cpfOuCnpj, TipoPessoa tipoPessoa) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio");
        }
        if (cpfOuCnpj == null || cpfOuCnpj.isBlank()) {
            throw new IllegalArgumentException("CPF ou CNPJ não pode ser nulo ou vazio");
        }
        if (tipoPessoa == null) {
            throw new IllegalArgumentException("Tipo de pessoa não pode ser nulo");
        }
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void atribuirId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpfOuCnpj() {
        return cpfOuCnpj;
    }

    public TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParteContraria that = (ParteContraria) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(cpfOuCnpj, that.cpfOuCnpj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cpfOuCnpj);
    }

    @Override
    public String toString() {
        return "ParteContraria{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cpfOuCnpj='" + cpfOuCnpj + '\'' +
                ", tipoPessoa=" + tipoPessoa +
                '}';
    }
}
