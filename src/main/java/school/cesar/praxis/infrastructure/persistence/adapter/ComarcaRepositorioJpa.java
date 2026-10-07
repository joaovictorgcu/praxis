package school.cesar.praxis.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.ComarcaRepositorio;
import school.cesar.praxis.domain.jurisdicao.Comarca;
import school.cesar.praxis.infrastructure.persistence.entity.ComarcaEntity;
import school.cesar.praxis.infrastructure.persistence.repository.ComarcaJpaRepository;

@Repository
public class ComarcaRepositorioJpa implements ComarcaRepositorio {

    private final ComarcaJpaRepository jpa;

    public ComarcaRepositorioJpa(ComarcaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Comarca salvar(Comarca cadastro) {
        ComarcaEntity entidade = new ComarcaEntity();
        entidade.setId(cadastro.id());
        entidade.setNome(cadastro.nome());
        entidade.setMunicipio(cadastro.municipio());
        entidade.setUf(cadastro.uf());
        entidade.setTribunal(cadastro.tribunal());
        entidade.setEndereco(cadastro.endereco());
        entidade.setTelefone(cadastro.telefone());
        entidade.setEmail(cadastro.email());
        entidade.setHorarioAtendimento(cadastro.horarioAtendimento());
        entidade.setObservacoes(cadastro.observacoes());
        entidade.setDataCadastro(cadastro.dataCadastro());
        return paraDominio(jpa.save(entidade));
    }

    @Override
    public Optional<Comarca> porId(Long id) {
        return jpa.findById(id).map(ComarcaRepositorioJpa::paraDominio);
    }

    @Override
    public List<Comarca> listarTodas() {
        return jpa.findAll().stream().map(ComarcaRepositorioJpa::paraDominio).toList();
    }

    private static Comarca paraDominio(ComarcaEntity entidade) {
        return new Comarca(
            entidade.getId(),
            entidade.getNome(),
            entidade.getMunicipio(),
            entidade.getUf(),
            entidade.getTribunal(),
            entidade.getEndereco(),
            entidade.getTelefone(),
            entidade.getEmail(),
            entidade.getHorarioAtendimento(),
            entidade.getObservacoes(),
            entidade.getDataCadastro()
        );
    }
}
