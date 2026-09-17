package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.cliente.Cliente;
import school.cesar.praxis.domain.compartilhado.TipoPessoa;

import java.util.List;
import java.util.Optional;

/** Porta de saida: persistencia do agregado Cliente. */
public interface ClienteRepositorio {

    Cliente salvar(Cliente cliente);

    Optional<Cliente> porId(Long id);

    /** Inclui inativos: a coluna e unica no banco, entao a checagem precisa ser igual. */
    Optional<Cliente> porCpfOuCnpj(String cpfOuCnpj);

    Optional<Cliente> porCpfOuCnpjAtivo(String cpfOuCnpj);

    List<Cliente> listarAtivos();

    List<Cliente> listarTodos();

    List<Cliente> porTipoAtivos(TipoPessoa tipo);

    List<Cliente> porCidadeAtivos(String cidade);

    List<Cliente> porEstadoAtivos(String estado);
}