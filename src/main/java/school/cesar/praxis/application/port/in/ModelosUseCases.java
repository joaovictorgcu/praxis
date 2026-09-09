package school.cesar.praxis.application.port.in;

import school.cesar.praxis.domain.documento.TipoDocumento;

import java.util.List;
import java.util.Set;

/**
 * Portas de entrada da funcionalidade <b>Cadastro de modelos de documento</b>.
 *
 * <p>O modelo define corpo e pedidos com marcadores {@code {{campo}}}; a ordem
 * das secoes da peca continua no dominio.
 */
public interface ModelosUseCases {

    /** Projecao de leitura do modelo, para tela e API. */
    record ItemModelo(Long id,
                      String codigo,
                      String nome,
                      TipoDocumento tipo,
                      String rotuloTipo,
                      String titulo,
                      boolean enderecaAoJuizo,
                      Set<String> camposEsperados) {
    }

    interface CadastrarModelo {

        record Comando(String codigo,
                       String nome,
                       TipoDocumento tipo,
                       String titulo,
                       String corpo,
                       String pedidos,
                       boolean enderecaAoJuizo) {
        }

        ItemModelo executar(Comando comando);
    }

    interface ListarModelos {

        List<ItemModelo> executar();
    }

    interface RemoverModelo {

        void executar(Long id);
    }
}
