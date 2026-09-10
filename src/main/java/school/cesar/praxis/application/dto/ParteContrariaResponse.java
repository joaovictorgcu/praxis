package school.cesar.praxis.application.dto;

import school.cesar.praxis.domain.partecontraria.TipoPessoa;

import java.time.LocalDateTime;

/**
 * DTO para resposta ao criar/consultar uma parte contrária.
 */
public class ParteContrariaResponse {

    private Long id;
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
    private boolean ativa;
    private LocalDateTime criadaEm;
    private LocalDateTime atualizadaEm;

    public ParteContrariaResponse() {}

    public ParteContrariaResponse(Long id, String nome, String cpfOuCnpj, TipoPessoa tipoPessoa,
                                 String email, String telefone, String endereco, String cidade,
                                 String estado, String cep, String observacoes, boolean ativa,
                                 LocalDateTime criadaEm, LocalDateTime atualizadaEm) {
        this.id = id;
        this.nome = nome;
        this.cpfOuCnpj = cpfOuCnpj;
        this.tipoPessoa = tipoPessoa;
        this.email = email;
        this.telefone = telefone;
        this.endereco = endereco;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
        this.observacoes = observacoes;
        this.ativa = ativa;
        this.criadaEm = criadaEm;
        this.atualizadaEm = atualizadaEm;
    }

    // Getters e Setters

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

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    public void setAtualizadaEm(LocalDateTime atualizadaEm) {
        this.atualizadaEm = atualizadaEm;
    }
}
