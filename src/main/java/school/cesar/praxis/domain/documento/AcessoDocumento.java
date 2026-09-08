package school.cesar.praxis.domain.documento;

/** Porta de autorizacao consultada pelo Proxy. */
public interface AcessoDocumento {

    /** Conteudo integral do documento. */
    String carregar(String documentoId);
}
