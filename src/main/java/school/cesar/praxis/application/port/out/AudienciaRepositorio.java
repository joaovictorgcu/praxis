package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.agenda.Audiencia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Porta de saida: persistencia do agregado Audiencia. */
public interface AudienciaRepositorio {

    Audiencia salvar(Audiencia audiencia);

    Optional<Audiencia> porId(Long id);

    Optional<Audiencia> porNumeroProcessoAtiva(String numeroProcesso);

    List<Audiencia> listarAtivas();

    List<Audiencia> listarTodas();

    List<Audiencia> porSalaAtivas(String sala);

    List<Audiencia> porPeriodoAtivas(LocalDateTime dataInicio, LocalDateTime dataFim);
}
