package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.ContratoHonorarioRepositorio;
import school.cesar.praxis.domain.honorario.ContratoHonorario;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.ContratoHonorarioJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class ContratoHonorarioRepositorioJpa implements ContratoHonorarioRepositorio {

    private final ContratoHonorarioJpaRepository jpa;

    public ContratoHonorarioRepositorioJpa(ContratoHonorarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ContratoHonorario salvar(ContratoHonorario contrato) {
        return PersistenciaMapper.paraDominio(jpa.save(PersistenciaMapper.paraEntidade(contrato)));
    }

    @Override
    public Optional<ContratoHonorario> porId(Long id) {
        return jpa.findById(id).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public List<ContratoHonorario> porProcesso(NumeroCnj numero) {
        return jpa.findByNumeroProcessoOrderByIdDesc(numero.valor()).stream()
                .map(PersistenciaMapper::paraDominio)
                .toList();
    }

    @Override
    public List<ContratoHonorario> listar() {
        return jpa.findAll().stream().map(PersistenciaMapper::paraDominio).toList();
    }
}
