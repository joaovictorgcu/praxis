package school.cesar.praxis.domain.cliente;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade de domínio que representa um cliente.
 * Um cliente é aquele que procura os serviços da firma de advocacia.
 * Responsabilidades:
 * - Manter os dados do cliente
 * - Validar regras de negócio
 */
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String cpfOuCnpj;

    @Column(name = "tipo_pessoa", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoPessoa tipoPessoa;

    @Column
    private String email;

    @Column
    private String telefone;

    @Column
    private String celular;

    @Column
    private String endereco;

    @Column
    private String cidade;

    @Column
    private String estado;

    @Column
    private String cep;

    @Column
    private String profissao;

    @Column
    private String empresaTrabalho;

    @Column(length = 500)
    private String observacoes;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    // Construtores

    public Cliente() {
        this.criadoEm = LocalDateTime.now();
    }

    public Cliente(String nome, String cpfOuCnpj, TipoPessoa tipoPessoa) {
        validarParametros(nome, cpfOuCnpj, tipoPessoa);
        this.nome = nome;
        this.cpfOuCnpj = cpfOuCnpj;
        this.tipoPessoa = tipoPessoa;
        this.criadoEm = LocalDateTime.now();
        this.ativo = true;
    }

    // Métodos de negócio

    /**
     * Desativa o cliente sem removê-lo do banco
     */
    public void desativar() {
        this.ativo = false;
        this.atualizadoEm = LocalDateTime.now();
    }

    /**
     * Reativa o cliente
     */
    public void reativar() {
        this.ativo = true;
        this.atualizadoEm = LocalDateTime.now();
    }

    /**
     * Atualiza os dados do cliente
     */
    public void atualizar(String nome, String email, String telefone, String celular,
                         String endereco, String cidade, String estado, String cep,
                         String profissao, String empresaTrabalho, String observacoes) {
        if (nome != null && !nome.isBlank()) {
            this.nome = nome;
        }
        this.email = email;
        this.telefone = telefone;
        this.celular = celular;
        this.endereco = endereco;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
        this.profissao = profissao;
        this.empresaTrabalho = empresaTrabalho;
        this.observacoes = observacoes;
        this.atualizadoEm = LocalDateTime.now();
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

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
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

    public String getProfissao() {
        return profissao;
    }

    public void setProfissao(String profissao) {
        this.profissao = profissao;
    }

    public String getEmpresaTrabalho() {
        return empresaTrabalho;
    }

    public void setEmpresaTrabalho(String empresaTrabalho) {
        this.empresaTrabalho = empresaTrabalho;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(id, cliente.id) &&
               Objects.equals(cpfOuCnpj, cliente.cpfOuCnpj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cpfOuCnpj);
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cpfOuCnpj='" + cpfOuCnpj + '\'' +
                ", tipoPessoa=" + tipoPessoa +
                '}';
    }
}
