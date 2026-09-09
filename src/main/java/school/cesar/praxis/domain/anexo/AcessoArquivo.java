package school.cesar.praxis.domain.anexo;

import school.cesar.praxis.domain.compartilhado.AcessoRestrito;

/** Contrato de leitura de anexo - implementado pelo real e pelo Proxy. */
public interface AcessoArquivo extends AcessoRestrito<ArquivoAnexo> {
}
