package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.PrazoEventoEntity;

import java.util.List;

/** Repositorio Spring Data da trilha de eventos dos prazos. */
public interface PrazoEventoJpaRepository extends JpaRepository<PrazoEventoEntity, Long> {

    /** Ordem de registro: o id cresce na ordem em que os fatos foram gravados. */
    List<PrazoEventoEntity> findByPrazoIdOrderByIdAsc(Long prazoId);

    long countByPrazoId(Long prazoId);
}
