package school.cesar.praxis.domain.modelo;

/**
 * <b>Interpreter</b>: o texto de um modelo e uma pequena linguagem com dois
 * termos - trecho literal e referencia a um campo. Cada termo sabe se
 * interpretar contra o contexto da peca, e o modelo apenas concatena o
 * resultado.
 */
public interface ExpressaoTexto {

    String interpretar(ContextoTexto contexto);
}
