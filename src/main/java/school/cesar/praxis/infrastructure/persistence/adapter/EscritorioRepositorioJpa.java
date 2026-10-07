package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.EscritorioRepositorio;
import school.cesar.praxis.domain.escritorio.Escritorio;
import school.cesar.praxis.infrastructure.persistence.entity.EscritorioEntity;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.EscritorioJpaRepository;

import java.util.Optional;

/** Adaptador de saida: escritorio em JPA. */
@Repository
public class EscritorioRepositorioJpa implements EscritorioRepositorio {

    private final EscritorioJpaRepository jpa;

    public EscritorioRepositorioJpa(EscritorioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Escritorio salvar(Escritorio escritorio) {
        EscritorioEntity salvo = jpa.save(PersistenciaMapper.paraEntidade(escritorio));
        return PersistenciaMapper.paraDominio(salvo);
    }

    @Override
    public Optional<Escritorio> porId(Long id) {
        return jpa.findById(id).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public Optional<Escritorio> porEmail(String email) {
        return jpa.findByEmail(Escritorio.normalizarEmail(email)).map(PersistenciaMapper::paraDominio);
    }
}
