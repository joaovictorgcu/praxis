package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.cesar.praxis.infrastructure.persistence.entity.AdvogadoEntity;

import java.util.List;
import java.util.Optional;

public interface AdvogadoJpaRepository extends JpaRepository<AdvogadoEntity, Long> {

    Optional<AdvogadoEntity> findByOab(String oab);

    List<AdvogadoEntity> findByIdIn(List<Long> ids);

    @Query("SELECT a FROM AdvogadoEntity a WHERE "
            + "LOWER(a.nome) LIKE LOWER(CONCAT('%', :termo, '%')) "
            + "OR LOWER(a.oab) LIKE LOWER(CONCAT('%', :termo, '%')) "
            + "OR LOWER(a.especialidade) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<AdvogadoEntity> buscar(@Param("termo") String termo);
}
