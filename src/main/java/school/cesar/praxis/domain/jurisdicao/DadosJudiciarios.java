package school.cesar.praxis.domain.jurisdicao;

import java.util.Locale;
import java.util.Set;

final class DadosJudiciarios {

    private static final Set<String> UFS = Set.of(
        "AC",
        "AL",
        "AP",
        "AM",
        "BA",
        "CE",
        "DF",
        "ES",
        "GO",
        "MA",
        "MT",
        "MS",
        "MG",
        "PA",
        "PB",
        "PR",
        "PE",
        "PI",
        "RJ",
        "RN",
        "RS",
        "RO",
        "RR",
        "SC",
        "SP",
        "SE",
        "TO"
    );

    private DadosJudiciarios() {}

    static String obrigatorio(String valor, String campo, int limite) {
        String texto = opcional(valor, campo, limite);
        if (texto == null) {
            throw new IllegalArgumentException(campo + " é obrigatório.");
        }
        return texto;
    }

    static String opcional(String valor, String campo, int limite) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String texto = valor.trim();
        if (texto.length() > limite) {
            throw new IllegalArgumentException(campo + " deve ter até " + limite + " caracteres.");
        }
        return texto;
    }

    static String uf(String valor) {
        String uf = obrigatorio(valor, "Estado", 2).toUpperCase(Locale.ROOT);
        if (!UFS.contains(uf)) {
            throw new IllegalArgumentException("Selecione um estado válido.");
        }
        return uf;
    }

    static String email(String valor) {
        String email = opcional(valor, "E-mail", 160);
        if (email != null && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
        return email;
    }
}
