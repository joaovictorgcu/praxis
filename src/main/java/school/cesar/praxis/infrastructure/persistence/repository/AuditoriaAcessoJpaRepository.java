package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.AuditoriaAcessoEntity;

import java.util.List;

/** Repositorio Spring Data da auditoria de acesso (detalhe de infraestrutura). */
public interface AuditoriaAcessoJpaRepository extends JpaRepository<AuditoriaAcessoEntity, Long> {

    List<AuditoriaAcessoEntity> findAllByOrderByQuandoDesc();
}
