package school.cesar.praxis.domain.compartilhado;

/**
 * <b>Proxy</b> de protecao: intercepta a leitura e verifica o segredo de
 * justica (art. 189 do CPC) antes de devolver o conteudo. O objeto real (o
 * adaptador de persistencia) so e consultado quando ha um solicitante, e o
 * conteudo so sai daqui se a OAB estiver habilitada nos autos.
 *
 * <p>A regra e escrita uma vez e vale para todo {@link ConteudoRestrito} -
 * peca gerada ou arquivo anexado. {@code carregar} e {@code final} porque a
 * checagem nao e opcional para quem especializa o Proxy.
 */
public class ProxyDeAcesso<T extends ConteudoRestrito> implements AcessoRestrito<T> {

    private final AcessoRestrito<T> real;
    private final String oabSolicitante;
    /** Como o conteudo se chama na mensagem de erro: "documento", "arquivo". */
    private final String substantivo;

    protected ProxyDeAcesso(AcessoRestrito<T> real, String oabSolicitante, String substantivo) {
        this.real = real;
        this.oabSolicitante = oabSolicitante;
        this.substantivo = substantivo;
    }

    @Override
    public final T carregar(Long id) {
        if (oabSolicitante == null || oabSolicitante.isBlank()) {
            throw new AcessoNegadoException("solicitante nao identificado por OAB");
        }
        T conteudo = real.carregar(id);
        if (!conteudo.podeSerLidoPor(oabSolicitante)) {
            throw new AcessoNegadoException(
                    substantivo + " " + id + " esta em segredo de justica e a OAB "
                            + oabSolicitante + " nao esta habilitada nos autos");
        }
        return conteudo;
    }

    /** Falha de dominio: acesso barrado pelo segredo de justica. */
    public static class AcessoNegadoException extends RuntimeException {
        public AcessoNegadoException(String mensagem) {
            super(mensagem);
        }
    }
}
