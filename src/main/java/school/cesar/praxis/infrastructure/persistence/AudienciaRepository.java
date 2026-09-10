package school.cesar.praxis.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.cesar.praxis.domain.agenda.Audiencia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para persistência de Audiências.
 * Responsável por operações de CRUD no banco de dados.
 */
@Repository
public interface AudienciaRepository extends JpaRepository<Audiencia, Long> {

    /**
     * Busca audiência ativa pelo número do processo
     */
    Optional<Audiencia> findByNumeroProcessoAndAtivaTrue(String numeroProcesso);

    /**
     * Lista todas as audiências ativas
     */
    List<Audiencia> findByAtivaTrue();

    /**
     * Lista audiências ativas por sala
     */
    List<Audiencia> findBySalaAndAtivaTrueOrderByDataHoraInicio(String sala);

    /**
     * Busca audiências ativas que se sobrepõem em período e sala
     * Usada para detectar conflitos de horário
     */
    @Query("""
        SELECT a FROM Audiencia a 
        WHERE a.ativa = true 
        AND a.sala = :sala 
        AND a.dataHoraInicio < :dataFim 
        AND a.dataHoraFim > :dataInicio
        AND a.id != :idAudienciaAtual
        ORDER BY a.dataHoraInicio
        """)
    List<Audiencia> encontrarConflitosDeHorario(
        @Param("sala") String sala,
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim,
        @Param("idAudienciaAtual") Long idAudienciaAtual
    );

    /**
     * Lista audiências dentro de um período de datas
     */
    @Query("""
        SELECT a FROM Audiencia a 
        WHERE a.ativa = true 
        AND a.dataHoraInicio >= :dataInicio 
        AND a.dataHoraFim <= :dataFim
        ORDER BY a.dataHoraInicio
        """)
    List<Audiencia> encontrarPorPeriodo(
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim
    );

    /**
     * Conta audiências ativas em um período específico e sala
     */
    @Query("""
        SELECT COUNT(a) FROM Audiencia a 
        WHERE a.ativa = true 
        AND a.sala = :sala
        AND DATE(a.dataHoraInicio) = DATE(:data)
        """)
    long contarAudienciasPorDiaESala(
        @Param("sala") String sala,
        @Param("data") LocalDateTime data
    );
}
