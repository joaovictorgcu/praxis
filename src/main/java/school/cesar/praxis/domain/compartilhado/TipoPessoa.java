package school.cesar.praxis.domain.compartilhado;

public enum TipoPessoa {
    FISICA("Pessoa Fisica"),
    JURIDICA("Pessoa Juridica");

    private final String descricao;

    TipoPessoa(String descricao) {
        this.descricao = descricao;
    }

    public static TipoPessoa de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("tipo de pessoa nao pode ser nulo ou vazio");
        }
        try {
            return TipoPessoa.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("tipo de pessoa invalido: " + valor);
        }
    }

    public String getDescricao() {
        return descricao;
    }
}