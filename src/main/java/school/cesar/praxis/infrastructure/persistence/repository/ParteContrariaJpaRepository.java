package school.cesar.praxis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.cesar.praxis.domain.compartilhado.TipoPessoa;
import school.cesar.praxis.infrastructure.persistence.entity.ParteContrariaEntity;

import java.util.List;
import java.util.Optional;

/** Repositorio Spring Data da parte contraria (detalhe de infraestrutura). */
public interface ParteContrariaJpaRepository extends JpaRepository<ParteContrariaEntity, Long> {

    Optional<ParteContrariaEntity> findByCpfOuCnpjAndAtivaTrue(String cpfOuCnpj);

    List<ParteContrariaEntity> findByAtivaTrue();

    List<ParteContrariaEntity> findByTipoPessoaAndAtivaTrue(TipoPessoa tipoPessoa);

    List<ParteContrariaEntity> findByCidadeAndAtivaTrue(String cidade);
}