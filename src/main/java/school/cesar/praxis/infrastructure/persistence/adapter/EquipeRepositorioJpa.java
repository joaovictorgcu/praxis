package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.EquipeRepositorio;
import school.cesar.praxis.domain.equipe.Equipe;
import school.cesar.praxis.infrastructure.persistence.entity.EquipeEntity;
import school.cesar.praxis.infrastructure.persistence.repository.EquipeJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class EquipeRepositorioJpa implements EquipeRepositorio {

    private final EquipeJpaRepository jpa;

    public EquipeRepositorioJpa(EquipeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Equipe salvar(Equipe equipe) {
        EquipeEntity entidade = new EquipeEntity();
        entidade.setId(equipe.getId());
        entidade.setNome(equipe.getNome());
        entidade.setMembrosIds(equipe.getMembrosIds());
        return paraDominio(jpa.save(entidade));
    }

    @Override
    public Optional<Equipe> porId(Long id) {
        return jpa.findById(id).map(EquipeRepositorioJpa::paraDominio);
    }

    @Override
    public List<Equipe> listarTodas() {
        return jpa.findAll().stream().map(EquipeRepositorioJpa::paraDominio).toList();
    }

    private static Equipe paraDominio(EquipeEntity entidade) {
        return new Equipe(entidade.getId(), entidade.getNome(), entidade.getMembrosIds());
    }
}
