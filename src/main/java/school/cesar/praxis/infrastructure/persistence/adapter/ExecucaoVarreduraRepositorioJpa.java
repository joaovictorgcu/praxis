package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.ExecucaoVarreduraRepositorio;
import school.cesar.praxis.domain.prazo.ExecucaoVarredura;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.VarreduraExecucaoJpaRepository;

import java.util.List;

/** Adaptador de saida: historico de execucoes da varredura em JPA ({@code varredura_execucao}). */
@Repository
public class ExecucaoVarreduraRepositorioJpa implements ExecucaoVarreduraRepositorio {

    private final VarreduraExecucaoJpaRepository jpa;

    public ExecucaoVarreduraRepositorioJpa(VarreduraExecucaoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ExecucaoVarredura registrar(ExecucaoVarredura execucao) {
        return PersistenciaMapper.paraDominio(jpa.save(PersistenciaMapper.paraEntidade(execucao)));
    }

    @Override
    public List<ExecucaoVarredura> ultimas(int limite) {
        return jpa.findAllByOrderByIniciadaEmDescIdDesc(PageRequest.of(0, limite)).stream()
                .map(PersistenciaMapper::paraDominio)
                .toList();
    }
}
