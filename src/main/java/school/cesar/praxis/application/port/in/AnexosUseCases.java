package school.cesar.praxis.application.port.in;

import school.cesar.praxis.domain.anexo.ArquivoAnexo;
import school.cesar.praxis.domain.anexo.TipoArquivo;

import java.time.LocalDate;
import java.util.List;

/**
 * Portas de entrada da funcionalidade <b>Upload e anexacao de arquivos ao
 * processo</b>.
 *
 * <p>Nao existe caso de uso de remocao: documento juntado aos autos nao se
 * desanexa. Retirar peca dos autos depende de decisao judicial
 * (desentranhamento), que nao e operacao de tela.
 */
public interface AnexosUseCases {

    /** Projecao de leitura do anexo: sem os bytes, que so saem pelo Proxy. */
    record ItemAnexo(Long id,
                     String numeroProcesso,
                     String nome,
                     TipoArquivo tipo,
                     String rotuloTipo,
                     int tamanhoBytes,
                     String descricao,
                     LocalDate anexadoEm,
                     String anexadoPorOab,
                     boolean segredoJustica) {
    }

    interface AnexarArquivo {

        record Comando(String numeroProcesso,
                       String nomeArquivo,
                       String tipoConteudo,
                       byte[] conteudo,
                       String descricao,
                       String oabSolicitante) {
        }

        ItemAnexo executar(Comando comando);
    }

    interface ListarAnexos {

        List<ItemAnexo> executar(String numeroProcesso);
    }

    /** Leitura protegida: passa pelo Proxy do segredo de justica. */
    interface BaixarAnexo {

        ArquivoAnexo executar(Long anexoId, String oabSolicitante);
    }
}
