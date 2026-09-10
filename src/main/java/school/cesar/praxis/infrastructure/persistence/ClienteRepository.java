package school.cesar.praxis.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.cesar.praxis.domain.cliente.Cliente;
import school.cesar.praxis.domain.cliente.TipoPessoa;

import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para persistência de Clientes.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca cliente ativo por CPF/CNPJ
     */
    Optional<Cliente> findByCpfOuCnpjAndAtivoTrue(String cpfOuCnpj);

    /**
     * Busca cliente ativo por nome
     */
    Optional<Cliente> findByNomeAndAtivoTrue(String nome);

    /**
     * Lista todos os clientes ativos
     */
    List<Cliente> findByAtivoTrue();

    /**
     * Lista clientes ativos por tipo (Física ou Jurídica)
     */
    List<Cliente> findByTipoPessoaAndAtivoTrue(TipoPessoa tipoPessoa);

    /**
     * Lista clientes ativos por cidade
     */
    List<Cliente> findByCidadeAndAtivoTrue(String cidade);

    /**
     * Lista clientes ativos por estado
     */
    List<Cliente> findByEstadoAndAtivoTrue(String estado);
}
