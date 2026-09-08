package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.PrazoRepositorio;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.PrazoJpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Adaptador de saida: agenda de prazos em JPA. */
@Repository
public class PrazoRepositorioJpa implements PrazoRepositorio {

    private final PrazoJpaRepository jpa;

    public PrazoRepositorioJpa(PrazoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Prazo salvar(Prazo prazo) {
        return PersistenciaMapper.paraDominio(jpa.save(PersistenciaMapper.paraEntidade(prazo)));
    }

    @Override
    public Optional<Prazo> porId(Long id) {
        return jpa.findById(id).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public List<Prazo> emAberto() {
        return jpa.findByCumpridoFalse().stream().map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    public List<Prazo> porProcesso(NumeroCnj numero) {
        return jpa.findByNumeroProcesso(numero.valor()).stream()
                .map(PersistenciaMapper::paraDominio)
                .toList();
    }

    @Override
    public List<Prazo> agendaAte(LocalDate limite) {
        return jpa.agendaAte(limite).stream().map(PersistenciaMapper::paraDominio).toList();
    }
}
