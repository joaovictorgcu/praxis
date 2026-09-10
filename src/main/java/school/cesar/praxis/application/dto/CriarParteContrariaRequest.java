package school.cesar.praxis.application.dto;

import school.cesar.praxis.domain.partecontraria.TipoPessoa;

/**
 * DTO para criar/atualizar uma parte contrária.
 */
public class CriarParteContrariaRequest {

    private String nome;
    private String cpfOuCnpj;
    private TipoPessoa tipoPessoa;
    private String email;
    private String telefone;
    private String endereco;
    private String cidade;
    private String estado;
    private String cep;
    private String observacoes;

    public CriarParteContrariaRequest() {}

    public CriarParteContrariaRequest(String nome, String cpfOuCnpj, TipoPessoa tipoPessoa) {
        this.nome = nome;
        this.cpfOuCnpj = cpfOuCnpj;
        this.tipoPessoa = tipoPessoa;
    }

    // Getters e Setters

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpfOuCnpj() {
        return cpfOuCnpj;
    }

    public void setCpfOuCnpj(String cpfOuCnpj) {
        this.cpfOuCnpj = cpfOuCnpj;
    }

    public TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(TipoPessoa tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
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
}
