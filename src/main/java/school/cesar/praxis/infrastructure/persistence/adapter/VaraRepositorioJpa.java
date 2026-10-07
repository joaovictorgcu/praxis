package school.cesar.praxis.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.VaraRepositorio;
import school.cesar.praxis.domain.jurisdicao.VaraJudicial;
import school.cesar.praxis.infrastructure.persistence.entity.VaraEntity;
import school.cesar.praxis.infrastructure.persistence.repository.VaraJpaRepository;

@Repository
public class VaraRepositorioJpa implements VaraRepositorio {

    private final VaraJpaRepository jpa;

    public VaraRepositorioJpa(VaraJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public VaraJudicial salvar(VaraJudicial cadastro) {
        VaraEntity entidade = new VaraEntity();
        entidade.setId(cadastro.id());
        entidade.setComarcaId(cadastro.comarcaId());
        entidade.setNome(cadastro.nome());
        entidade.setCompetencia(cadastro.competencia());
        entidade.setEndereco(cadastro.endereco());
        entidade.setTelefone(cadastro.telefone());
        entidade.setEmail(cadastro.email());
        entidade.setHorarioAtendimento(cadastro.horarioAtendimento());
        entidade.setObservacoes(cadastro.observacoes());
        entidade.setDataCadastro(cadastro.dataCadastro());
        return paraDominio(jpa.save(entidade));
    }

    @Override
    public Optional<VaraJudicial> porId(Long id) {
        return jpa.findById(id).map(VaraRepositorioJpa::paraDominio);
    }

    @Override
    public List<VaraJudicial> listarTodas() {
        return jpa.findAll().stream().map(VaraRepositorioJpa::paraDominio).toList();
    }

    private static VaraJudicial paraDominio(VaraEntity entidade) {
        return new VaraJudicial(
            entidade.getId(),
            entidade.getComarcaId(),
            entidade.getNome(),
            entidade.getCompetencia(),
            entidade.getEndereco(),
            entidade.getTelefone(),
            entidade.getEmail(),
            entidade.getHorarioAtendimento(),
            entidade.getObservacoes(),
            entidade.getDataCadastro()
        );
    }
}
