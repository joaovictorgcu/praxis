package school.cesar.praxis.domain.cliente;

import school.cesar.praxis.domain.compartilhado.TipoPessoa;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade de domínio que representa um cliente.
 * Um cliente é aquele que procura os serviços da firma de advocacia.
 * Responsabilidades:
 * - Manter os dados do cliente
 * - Validar regras de negócio
 */

public class Cliente {

    private Long id;

    private String nome;

    private String cpfOuCnpj;

    private TipoPessoa tipoPessoa;

    private String email;

    private String telefone;

    private String celular;

    private String endereco;

    private String cidade;

    private String estado;

    private String cep;

    private String profissao;

    private String empresaTrabalho;

    private String observacoes;

    private boolean ativo = true;

    private LocalDateTime criadoEm;

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

    /** Construtor de restauracao: usado pelo mapper para reidratar um cliente ja persistido. */
    public Cliente(Long id, String nome, String cpfOuCnpj, TipoPessoa tipoPessoa, String email,
                String telefone, String celular, String endereco, String cidade, String estado,
                String cep, String profissao, String empresaTrabalho, String observacoes,
                boolean ativo, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.nome = nome;
        this.cpfOuCnpj = cpfOuCnpj;
        this.tipoPessoa = tipoPessoa;
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
        this.ativo = ativo;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
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
