package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.prazo.EventoPrazo;

import java.util.List;

/** Porta de saida: a trilha de eventos do prazo ({@code prazo_evento}). So acrescenta. */
public interface HistoricoDePrazoRepositorio {

    EventoPrazo registrar(EventoPrazo evento);

    /** Eventos do prazo na ordem em que foram registrados. */
    List<EventoPrazo> doPrazo(Long prazoId);
}
