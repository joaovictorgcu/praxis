package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.cesar.praxis.infrastructure.persistence.entity.PrazoEntity;

import java.time.LocalDate;
import java.util.List;

/** Repositorio Spring Data da agenda de prazos. */
public interface PrazoJpaRepository extends JpaRepository<PrazoEntity, Long> {

    List<PrazoEntity> findByCumpridoFalse();

    List<PrazoEntity> findByNumeroProcesso(String numeroProcesso);

    @Query("select p from PrazoEntity p where p.cumprido = false and p.vencimento <= :limite")
    List<PrazoEntity> agendaAte(@Param("limite") LocalDate limite);
}
