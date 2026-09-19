package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.AudienciaEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Repositorio Spring Data da audiencia (detalhe de infraestrutura). */
public interface AudienciaJpaRepository extends JpaRepository<AudienciaEntity, Long> {

    Optional<AudienciaEntity> findByNumeroProcessoAndAtivaTrue(String numeroProcesso);

    List<AudienciaEntity> findByAtivaTrue();

    List<AudienciaEntity> findBySalaAndAtivaTrueOrderByDataHoraInicio(String sala);

    List<AudienciaEntity> findByAtivaTrueAndDataHoraInicioGreaterThanEqualAndDataHoraFimLessThanEqualOrderByDataHoraInicio(
            LocalDateTime dataInicio, LocalDateTime dataFim);
}
