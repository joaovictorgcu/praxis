package school.cesar.praxis.domain.notificacao;

import school.cesar.praxis.domain.prazo.NivelAlerta;
import school.cesar.praxis.domain.processo.Advogado;

import java.time.LocalDate;

/** Eventos de dominio publicados pelos agregados e observados por interessados. */
public sealed interface EventoProcesso {

    String numeroProcesso();

    Advogado destinatario();

    record AndamentoRegistrado(String numeroProcesso,
                               Advogado destinatario,
                               String descricao,
                               LocalDate data) implements EventoProcesso {
    }

    record PrazoEmRisco(String numeroProcesso,
                        Advogado destinatario,
                        String descricaoPrazo,
                        LocalDate vencimento,
                        int diasRestantes,
                        NivelAlerta nivel) implements EventoProcesso {
    }

    record PrazoVencido(String numeroProcesso,
                        Advogado destinatario,
                        String descricaoPrazo,
                        LocalDate vencimento) implements EventoProcesso {
    }

    record DocumentoGerado(String numeroProcesso,
                           Advogado destinatario,
                           String tipoDocumento) implements EventoProcesso {
    }

    record ArquivoAnexado(String numeroProcesso,
                          Advogado destinatario,
                          String nomeArquivo) implements EventoProcesso {
    }
}
