package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.AdvogadoRepositorio;
import school.cesar.praxis.domain.advogado.Advogado;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.AdvogadoJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class AdvogadoRepositorioJpa implements AdvogadoRepositorio {

    private final AdvogadoJpaRepository jpa;

    public AdvogadoRepositorioJpa(AdvogadoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Advogado salvar(Advogado advogado) {
        return PersistenciaMapper.paraDominio(jpa.save(PersistenciaMapper.paraEntidade(advogado)));
    }

    @Override
    public Optional<Advogado> porOab(String oab) {
        return jpa.findByOab(oab).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public Optional<Advogado> porId(Long id) {
        return jpa.findById(id).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public List<Advogado> listarTodos() {
        return jpa.findAll().stream().map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    public List<Advogado> buscar(String termo) {
        return jpa.buscar(termo).stream().map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    public List<Advogado> listarPorIds(List<Long> ids) {
        return jpa.findByIdIn(ids).stream().map(PersistenciaMapper::paraDominio).toList();
    }
}
