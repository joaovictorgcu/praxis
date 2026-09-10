package school.cesar.praxis.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.cesar.praxis.domain.partecontraria.ParteContraria;
import school.cesar.praxis.domain.partecontraria.TipoPessoa;

import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para persistência de Partes Contrárias.
 */
@Repository
public interface ParteContrariaRepository extends JpaRepository<ParteContraria, Long> {

    /**
     * Busca parte contrária ativa por CPF/CNPJ
     */
    Optional<ParteContraria> findByCpfOuCnpjAndAtivaTrue(String cpfOuCnpj);

    /**
     * Busca parte contrária ativa por nome
     */
    Optional<ParteContraria> findByNomeAndAtivaTrue(String nome);

    /**
     * Lista todas as partes contrárias ativas
     */
    List<ParteContraria> findByAtivaTrue();

    /**
     * Lista partes contrárias ativas por tipo (Física ou Jurídica)
     */
    List<ParteContraria> findByTipoPessoaAndAtivaTrue(TipoPessoa tipoPessoa);

    /**
     * Busca partes contrárias ativas por cidade
     */
    List<ParteContraria> findByCidadeAndAtivaTrue(String cidade);
}
