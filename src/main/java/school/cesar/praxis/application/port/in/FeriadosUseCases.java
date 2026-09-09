package school.cesar.praxis.application.port.in;

import school.cesar.praxis.domain.feriado.Abrangencia;

import java.time.LocalDate;
import java.util.List;

/**
 * Portas de entrada da funcionalidade <b>Cadastro de feriados</b>.
 *
 * <p>O usuario informa uma data e diz se ela repete todo ano; a camada de
 * aplicacao traduz isso na {@code RegraRecorrencia} correspondente.
 */
public interface FeriadosUseCases {

    /**
     * Projecao de leitura do feriado, para tela e API.
     *
     * <p>Em {@code dataDeReferencia}, o ano so tem significado quando o feriado
     * <b>nao</b> repete todo ano; na recorrencia anual valem apenas dia e mes.
     */
    record ItemFeriado(Long id,
                       String descricao,
                       LocalDate dataDeReferencia,
                       boolean repeteTodoAno,
                       String recorrencia,
                       String abrangencia) {
    }

    interface CadastrarFeriado {

        record Comando(String descricao,
                       LocalDate data,
                       boolean repeteTodoAno,
                       Abrangencia.Nivel nivel,
                       String abrangencia) {
        }

        ItemFeriado executar(Comando comando);
    }

    interface ListarFeriados {

        List<ItemFeriado> executar();
    }

    interface RemoverFeriado {

        void executar(Long id);
    }

    /**
     * Consulta o calendario ja montado: corre prazo neste dia, neste foro?
     * Existe para tornar o efeito do cadastro observavel de fora.
     */
    interface ConsultarDiaUtil {

        record Resposta(LocalDate data, boolean diaUtil, LocalDate proximoDiaUtil) {
        }

        Resposta executar(LocalDate data);
    }
}
