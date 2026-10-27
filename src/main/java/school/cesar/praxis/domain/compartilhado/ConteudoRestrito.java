package school.cesar.praxis.domain.compartilhado;

import school.cesar.praxis.domain.processo.NumeroCnj;

/**
 * Nucleo compartilhado: conteudo dos autos cujo acesso e restrito pelo segredo
 * de justica (CPC art. 189).
 *
 * <p>Peca gerada pelo sistema e arquivo recebido de fora sao coisas
 * diferentes, mas obedecem a mesma regra de acesso. Quem decide se a OAB
 * esta habilitada NESTA peca/anexo e o proprio conteudo; a decisao completa
 * (incluindo acesso concedido ao processo inteiro) e da
 * {@link PoliticaDeLeituraDosAutos}, usada pelo {@link ProxyDeAcesso}.
 */
public interface ConteudoRestrito {

    boolean podeSerLidoPor(String oabSolicitante);

    /** De qual processo este conteudo e: a politica de acesso ao processo precisa saber. */
    NumeroCnj getNumeroProcesso();
}
