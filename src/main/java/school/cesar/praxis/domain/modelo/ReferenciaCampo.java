package school.cesar.praxis.domain.modelo;

/**
 * Termo da linguagem: referencia a um campo, escrita como {@code {{nome}}}.
 *
 * <p>Campo nao informado nao quebra a geracao: vira um marcador visivel na
 * peca, no mesmo espirito dos "(fatos a preencher)" dos geradores compilados.
 * Peca com lacuna aparente e melhor que peca com lacuna silenciosa.
 */
public record ReferenciaCampo(String nome) implements ExpressaoTexto {

    public ReferenciaCampo {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("referencia sem nome de campo");
        }
    }

    @Override
    public String interpretar(ContextoTexto contexto) {
        return contexto.valor(nome).orElse("(" + nome + " a preencher)");
    }
}
