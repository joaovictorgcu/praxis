package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.ProcessoRepositorio;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.domain.processo.Processo;
import school.cesar.praxis.infrastructure.persistence.entity.ProcessoEntity;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.ProcessoJpaRepository;

import java.util.List;
import java.util.Optional;

/** Adaptador de saida: implementa a porta com JPA. */
@Repository
public class ProcessoRepositorioJpa implements ProcessoRepositorio {

    private final ProcessoJpaRepository jpa;

    public ProcessoRepositorioJpa(ProcessoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Processo salvar(Processo processo) {
        ProcessoEntity salvo = jpa.save(PersistenciaMapper.paraEntidade(processo));
        return PersistenciaMapper.paraDominio(salvo);
    }

    @Override
    public Optional<Processo> porNumero(NumeroCnj numero) {
        return jpa.findByNumeroCnj(numero.valor()).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public List<Processo> listar() {
        return jpa.findAll().stream().map(PersistenciaMapper::paraDominio).toList();
    }
}
