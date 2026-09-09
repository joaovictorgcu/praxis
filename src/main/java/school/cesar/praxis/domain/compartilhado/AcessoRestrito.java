package school.cesar.praxis.domain.compartilhado;

/** Contrato de leitura de conteudo restrito - implementado pelo real e pelo Proxy. */
public interface AcessoRestrito<T extends ConteudoRestrito> {

    T carregar(Long id);
}
