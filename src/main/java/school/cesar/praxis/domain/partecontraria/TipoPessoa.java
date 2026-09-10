package school.cesar.praxis.domain.partecontraria;

/**
 * Enumeração para tipo de pessoa.
 * Pode ser Física (CPF) ou Jurídica (CNPJ).
 */
public enum TipoPessoa {
    FISICA("Pessoa Física"),
    JURIDICA("Pessoa Jurídica");

    private final String descricao;

    TipoPessoa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
