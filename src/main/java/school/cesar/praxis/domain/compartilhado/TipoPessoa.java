package school.cesar.praxis.domain.compartilhado;

public enum TipoPessoa {
    FISICA("Pessoa Física"),
    JURIDICA("Pessoa Jurídica");

    private final String descricao;

    TipoPessoa(String descricao) {
        this.descricao = descricao;
    }

    public static TipoPessoa de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("tipo de pessoa não pode ser nulo ou vazio");
        }
        try {
            return TipoPessoa.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("tipo de pessoa inválido: " + valor);
        }
    }

    public String getDescricao() {
        return descricao;
    }
}