package school.cesar.praxis.application.port.in;

import school.cesar.praxis.domain.processo.Andamento;
import school.cesar.praxis.domain.processo.Processo;
import school.cesar.praxis.domain.processo.TipoAndamento;

import java.time.LocalDate;
import java.util.List;

/** Portas de entrada do subdominio nuclear Gestao de Processos. */
public interface ProcessosUseCases {

    interface CadastrarProcesso {

        record Comando(String numeroCnj,
                       String cliente,
                       String comarca,
                       boolean segredoJustica,
                       String responsavelNome,
                       String responsavelEmail,
                       String responsavelOab) {
        }

        Processo executar(Comando comando);
    }

    interface RegistrarAndamento {

        record Comando(String numeroCnj,
                       LocalDate data,
                       String descricao,
                       TipoAndamento tipo) {
        }

        Processo executar(Comando comando);
    }

    interface ConsultarLinhaDoTempo {

        List<Andamento> executar(String numeroCnj);
    }
}
