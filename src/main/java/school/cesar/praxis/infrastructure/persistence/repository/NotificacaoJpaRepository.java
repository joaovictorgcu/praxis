package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.NotificacaoEntity;

import java.util.List;

/** Repositorio Spring Data da trilha de auditoria de notificacoes. */
public interface NotificacaoJpaRepository extends JpaRepository<NotificacaoEntity, Long> {

    List<NotificacaoEntity> findByNumeroProcessoOrderByIdDesc(String numeroProcesso);
}
