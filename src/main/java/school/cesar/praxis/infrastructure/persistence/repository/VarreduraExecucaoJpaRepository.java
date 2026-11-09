package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.VarreduraExecucaoEntity;

import java.util.List;

/** Repositorio Spring Data do historico de execucoes da varredura. */
public interface VarreduraExecucaoJpaRepository extends JpaRepository<VarreduraExecucaoEntity, Long> {

    List<VarreduraExecucaoEntity> findAllByOrderByIniciadaEmDescIdDesc(Pageable pagina);
}
