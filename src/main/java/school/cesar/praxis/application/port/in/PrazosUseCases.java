package school.cesar.praxis.application.port.in;

import school.cesar.praxis.domain.prazo.AlertaPrazo;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.prazo.RegimeContagem;

import java.time.LocalDate;
import java.util.List;

/**
 * Portas de entrada da funcionalidade <b>Motor de prazos processuais com alertas</b>.
 * Agrupadas em um arquivo por serem contratos pequenos do mesmo caso de negocio.
 */
public interface PrazosUseCases {

    interface AbrirPrazo {

        record Comando(String numeroProcesso,
                       String descricao,
                       LocalDate intimacao,
                       int quantidadeDias,
                       boolean fatal,
                       RegimeContagem regime) {
        }

        Prazo executar(Comando comando);
    }

    interface CumprirPrazo {

        Prazo executar(Long prazoId);
    }

    /** Varredura da agenda: avalia prazos em aberto e dispara alertas. */
    interface VarrerPrazos {

        List<AlertaPrazo> executar(LocalDate hoje);

        List<AlertaPrazo> executarHoje();
    }

    interface ConsultarAgenda {

        record ItemAgenda(Long prazoId,
                          String numeroProcesso,
                          String descricao,
                          LocalDate vencimento,
                          int diasRestantes,
                          boolean fatal,
                          boolean vencido,
                          String responsavel) {
        }

        List<ItemAgenda> executar(LocalDate ate);
    }
}
