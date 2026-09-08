package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Porta de saida: persistencia e consultas do agregado Prazo. */
public interface PrazoRepositorio {

    Prazo salvar(Prazo prazo);

    Optional<Prazo> porId(Long id);

    List<Prazo> emAberto();

    List<Prazo> porProcesso(NumeroCnj numero);

    /** Agenda: prazos em aberto com vencimento ate a data informada. */
    List<Prazo> agendaAte(LocalDate limite);
}
