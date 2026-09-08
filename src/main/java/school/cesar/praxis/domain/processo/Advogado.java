package school.cesar.praxis.domain.processo;

/**
 * Value Object: advogado responsavel pelos autos. A OAB e o que o Proxy de
 * documento consulta para decidir acesso em segredo de justica.
 */
public record Advogado(String nome, String email, String oab) {

    public Advogado {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome do advogado e obrigatorio");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("e-mail do advogado invalido: " + email);
        }
        if (oab == null || oab.isBlank()) {
            throw new IllegalArgumentException("OAB e obrigatoria");
        }
    }
}
