package school.cesar.praxis.domain.documento;

import java.time.LocalDateTime;

/**
 * Uma versao do texto da peca, gravada em {@code documento_versao}. O historico
 * so cresce: restaurar a versao 2 cria uma versao nova com o texto da 2, em vez
 * de apagar as que vieram depois.
 */
public record VersaoDocumento(Long id,
                              Long documentoId,
                              int numero,
                              String conteudo,
                              OrigemVersao origem,
                              String autorOab,
                              String autorNome,
                              LocalDateTime criadaEm,
                              String resumo) {

    public VersaoDocumento {
        if (documentoId == null) {
            throw new IllegalArgumentException("versão exige a peça");
        }
        if (numero < 1) {
            throw new IllegalArgumentException("versão começa em 1");
        }
        if (conteudo == null || conteudo.isBlank()) {
            throw new IllegalArgumentException("versão sem conteúdo");
        }
        if (origem == null || criadaEm == null) {
            throw new IllegalArgumentException("versão exige origem e momento");
        }
    }

    public int caracteres() {
        return conteudo.length();
    }

    public int linhas() {
        return (int) conteudo.lines().count();
    }
}
