package school.cesar.praxis.domain.compartilhado;

/**
 * Nucleo compartilhado: conteudo dos autos cujo acesso e restrito pelo segredo
 * de justica (CPC art. 189).
 *
 * <p>Peca gerada pelo sistema e arquivo recebido de fora sao coisas
 * diferentes, mas obedecem a mesma regra de acesso. Quem decide se pode ser
 * lido e o proprio conteudo; o {@link ProxyDeAcesso} apenas faz cumprir.
 */
public interface ConteudoRestrito {

    boolean podeSerLidoPor(String oabSolicitante);
}
