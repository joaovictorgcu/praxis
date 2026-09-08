package school.cesar.praxis.domain.documento;

/** Contrato de leitura de documento - implementado pelo real e pelo Proxy. */
public interface AcessoDocumento {

    DocumentoGerado carregar(Long documentoId);
}
