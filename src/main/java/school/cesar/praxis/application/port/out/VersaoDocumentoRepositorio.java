package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.documento.VersaoDocumento;

import java.util.List;
import java.util.Optional;

/** Porta de saida: versoes do texto das pecas ({@code documento_versao}). */
public interface VersaoDocumentoRepositorio {

    VersaoDocumento registrar(VersaoDocumento versao);

    /** Versoes da peca em ordem crescente de numero. */
    List<VersaoDocumento> doDocumento(Long documentoId);

    Optional<VersaoDocumento> versao(Long documentoId, int numero);

    /** Usado so na exclusao do rascunho: as versoes vao junto com a peca. */
    void excluirDoDocumento(Long documentoId);

    long contar();
}
