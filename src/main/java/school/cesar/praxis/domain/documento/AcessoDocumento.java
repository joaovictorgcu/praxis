package school.cesar.praxis.domain.documento;

import school.cesar.praxis.domain.compartilhado.AcessoRestrito;

/** Contrato de leitura de documento - implementado pelo real e pelo Proxy. */
public interface AcessoDocumento extends AcessoRestrito<DocumentoGerado> {
}
