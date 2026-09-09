package school.cesar.praxis.domain.modelo;

/** Termo da linguagem: trecho fixo do modelo, que sai como esta. */
public record Literal(String texto) implements ExpressaoTexto {

    @Override
    public String interpretar(ContextoTexto contexto) {
        return texto;
    }
}
