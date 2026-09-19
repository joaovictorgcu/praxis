package school.cesar.praxis.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.AudienciaRepositorio;
import school.cesar.praxis.domain.agenda.Audiencia;
import school.cesar.praxis.infrastructure.persistence.mapper.PersistenciaMapper;
import school.cesar.praxis.infrastructure.persistence.repository.AudienciaJpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Adaptador de saida: audiencias em JPA. */
@Repository
public class AudienciaRepositorioJpa implements AudienciaRepositorio {

    private final AudienciaJpaRepository jpa;

    public AudienciaRepositorioJpa(AudienciaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Audiencia salvar(Audiencia audiencia) {
        return PersistenciaMapper.paraDominio(
                jpa.save(PersistenciaMapper.paraEntidade(audiencia)));
    }

    @Override
    public Optional<Audiencia> porId(Long id) {
        return jpa.findById(id).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public Optional<Audiencia> porNumeroProcessoAtiva(String numeroProcesso) {
        return jpa.findByNumeroProcessoAndAtivaTrue(numeroProcesso).map(PersistenciaMapper::paraDominio);
    }

    @Override
    public List<Audiencia> listarAtivas() {
        return jpa.findByAtivaTrue().stream().map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    public List<Audiencia> listarTodas() {
        return jpa.findAll().stream().map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    public List<Audiencia> porSalaAtivas(String sala) {
        return jpa.findBySalaAndAtivaTrueOrderByDataHoraInicio(sala)
                .stream().map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    public List<Audiencia> porPeriodoAtivas(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return jpa.findByAtivaTrueAndDataHoraInicioGreaterThanEqualAndDataHoraFimLessThanEqualOrderByDataHoraInicio(
                        dataInicio, dataFim)
                .stream().map(PersistenciaMapper::paraDominio).toList();
    }
}
