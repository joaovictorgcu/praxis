package school.cesar.praxis.application.port.in;

import school.cesar.praxis.application.dto.ClienteResponse;
import school.cesar.praxis.application.dto.CriarClienteRequest;
import school.cesar.praxis.domain.cliente.TipoPessoa;

import java.util.List;

/**
 * Port (Interface de Entrada) que define os casos de uso de Cliente.
 */
public interface ClienteUseCase {

    /**
     * Criar um novo cliente
     */
    ClienteResponse criarCliente(CriarClienteRequest request);

    /**
     * Consultar cliente por ID
     */
    ClienteResponse consultarCliente(Long id);

    /**
     * Consultar cliente por CPF/CNPJ
     */
    ClienteResponse consultarPorCpfOuCnpj(String cpfOuCnpj);

    /**
     * Listar todos os clientes ativos
     */
    List<ClienteResponse> listarClientes();

    /**
     * Listar clientes por tipo (Física ou Jurídica)
     */
    List<ClienteResponse> listarPorTipo(TipoPessoa tipo);

    /**
     * Listar clientes por cidade
     */
    List<ClienteResponse> listarPorCidade(String cidade);

    /**
     * Listar clientes por estado
     */
    List<ClienteResponse> listarPorEstado(String estado);

    /**
     * Editar um cliente existente
     */
    ClienteResponse editarCliente(Long id, CriarClienteRequest request);

    /**
     * Deletar (desativar) um cliente
     */
    void deletarCliente(Long id);

    /**
     * Reativar um cliente desativado
     */
    void reativarCliente(Long id);
}
