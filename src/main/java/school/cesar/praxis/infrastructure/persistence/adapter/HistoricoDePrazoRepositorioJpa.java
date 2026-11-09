package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.HistoricoDePrazoRepositorio;
import school.cesar.praxis.domain.prazo.EventoPrazo;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.PrazoEventoJpaRepository;

import java.util.List;

/** Adaptador de saida: trilha de eventos dos prazos em JPA ({@code prazo_evento}). */
@Repository
public class HistoricoDePrazoRepositorioJpa implements HistoricoDePrazoRepositorio {

    private final PrazoEventoJpaRepository jpa;

    public HistoricoDePrazoRepositorioJpa(PrazoEventoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public EventoPrazo registrar(EventoPrazo evento) {
        if (evento.id() != null) {
            throw new IllegalArgumentException("a trilha do prazo só acrescenta: evento já gravado (" + evento.id() + ")");
        }
        return PersistenciaMapper.paraDominio(jpa.save(PersistenciaMapper.paraEntidade(evento)));
    }

    @Override
    public List<EventoPrazo> doPrazo(Long prazoId) {
        return jpa.findByPrazoIdOrderByIdAsc(prazoId).stream()
                .map(PersistenciaMapper::paraDominio)
                .toList();
    }
}
