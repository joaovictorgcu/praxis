package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.anexo.AcessoArquivo;
import school.cesar.praxis.domain.anexo.ArquivoAnexo;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.util.List;

/**
 * Porta de saida: persistencia de anexos. Estende {@link AcessoArquivo} para
 * poder ser o objeto real por tras do {@code ArquivoProxy}.
 */
public interface ArquivoRepositorio extends AcessoArquivo {

    ArquivoAnexo salvar(ArquivoAnexo anexo);

    List<ArquivoAnexo> porProcesso(NumeroCnj numero);

    List<ArquivoAnexo> listar();
}
