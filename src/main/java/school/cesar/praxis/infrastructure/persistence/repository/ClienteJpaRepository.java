package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.domain.compartilhado.TipoPessoa;
import school.cesar.praxis.infrastructure.persistence.entity.ClienteEntity;

import java.util.List;
import java.util.Optional;

/** Repositorio Spring Data do cliente (detalhe de infraestrutura). */
public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, Long> {

    /** Inclui inativos: a coluna e unica no banco, entao a checagem precisa ser igual. */
    Optional<ClienteEntity> findByCpfOuCnpj(String cpfOuCnpj);

    Optional<ClienteEntity> findByCpfOuCnpjAndAtivoTrue(String cpfOuCnpj);

    List<ClienteEntity> findByAtivoTrue();

    List<ClienteEntity> findByTipoPessoaAndAtivoTrue(TipoPessoa tipoPessoa);

    List<ClienteEntity> findByCidadeAndAtivoTrue(String cidade);

    List<ClienteEntity> findByEstadoAndAtivoTrue(String estado);
}