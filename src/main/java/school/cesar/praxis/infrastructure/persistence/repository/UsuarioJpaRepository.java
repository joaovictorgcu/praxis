package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.infrastructure.persistence.entity.UsuarioEntity;

import java.util.Optional;

/** Repositorio Spring Data do usuario (detalhe de infraestrutura). */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByEmail(String email);

    Optional<UsuarioEntity> findByOab(String oab);
}
