package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.dto.ClienteResponse;
import school.cesar.praxis.application.dto.CriarClienteRequest;
import school.cesar.praxis.application.port.in.ClienteUseCase;
import school.cesar.praxis.domain.cliente.Cliente;
import school.cesar.praxis.domain.cliente.TipoPessoa;
import school.cesar.praxis.infrastructure.persistence.ClienteRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de Aplicação para Clientes.
 * Implementa os casos de uso definidos no port.
 */
@Service
@Transactional
public class ClienteAppService implements ClienteUseCase {

    private final ClienteRepository clienteRepository;

    public ClienteAppService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public ClienteResponse criarCliente(CriarClienteRequest request) {
        // Validar se já não existe outro cliente com o mesmo CPF/CNPJ
        // A coluna e unica no banco para ativos e inativos; checar so ativos deixaria
        // o insert estourar a constraint (500) apos um soft delete.
        clienteRepository.findByCpfOuCnpj(request.getCpfOuCnpj()).ifPresent(existente -> {
            throw new IllegalArgumentException(existente.isAtivo()
                ? "Já existe um cliente ativo com este CPF/CNPJ"
                : "Já existe um cliente inativo com este CPF/CNPJ (ID " + existente.getId() + "); reative-o");
        });

        Cliente novoCliente = new Cliente(
            request.getNome(),
            request.getCpfOuCnpj(),
            request.getTipoPessoa()
        );

        // Adicionar dados complementares
        if (request.getEmail() != null) {
            novoCliente.setEmail(request.getEmail());
        }
        if (request.getTelefone() != null) {
            novoCliente.setTelefone(request.getTelefone());
        }
        if (request.getCelular() != null) {
            novoCliente.setCelular(request.getCelular());
        }
        if (request.getEndereco() != null) {
            novoCliente.setEndereco(request.getEndereco());
        }
        if (request.getCidade() != null) {
            novoCliente.setCidade(request.getCidade());
        }
        if (request.getEstado() != null) {
            novoCliente.setEstado(request.getEstado());
        }
        if (request.getCep() != null) {
            novoCliente.setCep(request.getCep());
        }
        if (request.getProfissao() != null) {
            novoCliente.setProfissao(request.getProfissao());
        }
        if (request.getEmpresaTrabalho() != null) {
            novoCliente.setEmpresaTrabalho(request.getEmpresaTrabalho());
        }
        if (request.getObservacoes() != null) {
            novoCliente.setObservacoes(request.getObservacoes());
        }

        Cliente clienteSalvo = clienteRepository.save(novoCliente);
        return converterParaResponse(clienteSalvo);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .filter(Cliente::isAtivo)
            .orElse(null);
        
        return cliente != null ? converterParaResponse(cliente) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorCpfOuCnpj(String cpfOuCnpj) {
        Cliente cliente = clienteRepository.findByCpfOuCnpjAndAtivoTrue(cpfOuCnpj)
            .orElse(null);
        
        return cliente != null ? converterParaResponse(cliente) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarClientes() {
        return clienteRepository.findByAtivoTrue()
            .stream()
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarPorTipo(TipoPessoa tipo) {
        return clienteRepository.findByTipoPessoaAndAtivoTrue(tipo)
            .stream()
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarPorCidade(String cidade) {
        return clienteRepository.findByCidadeAndAtivoTrue(cidade)
            .stream()
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarPorEstado(String estado) {
        return clienteRepository.findByEstadoAndAtivoTrue(estado)
            .stream()
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }

    @Override
    public ClienteResponse editarCliente(Long id, CriarClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com ID: " + id));

        // Edicao parcial: campo ausente na requisicao mantem o valor atual.
        cliente.atualizar(
            request.getNome(),
            ou(request.getEmail(), cliente.getEmail()),
            ou(request.getTelefone(), cliente.getTelefone()),
            ou(request.getCelular(), cliente.getCelular()),
            ou(request.getEndereco(), cliente.getEndereco()),
            ou(request.getCidade(), cliente.getCidade()),
            ou(request.getEstado(), cliente.getEstado()),
            ou(request.getCep(), cliente.getCep()),
            ou(request.getProfissao(), cliente.getProfissao()),
            ou(request.getEmpresaTrabalho(), cliente.getEmpresaTrabalho()),
            ou(request.getObservacoes(), cliente.getObservacoes())
        );

        Cliente clienteAtualizado = clienteRepository.save(cliente);
        return converterParaResponse(clienteAtualizado);
    }

    @Override
    public void deletarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com ID: " + id));
        
        cliente.desativar();
        clienteRepository.save(cliente);
    }

    @Override
    public void reativarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com ID: " + id));
        
        if (cliente.isAtivo()) {
            throw new IllegalArgumentException("Cliente já está ativo");
        }
        
        cliente.reativar();
        clienteRepository.save(cliente);
    }

    // Métodos auxiliares

    private static String ou(String novo, String atual) {
        return novo != null ? novo : atual;
    }

    private ClienteResponse converterParaResponse(Cliente cliente) {
        return new ClienteResponse(
            cliente.getId(),
            cliente.getNome(),
            cliente.getCpfOuCnpj(),
            cliente.getTipoPessoa(),
            cliente.getEmail(),
            cliente.getTelefone(),
            cliente.getCelular(),
            cliente.getEndereco(),
            cliente.getCidade(),
            cliente.getEstado(),
            cliente.getCep(),
            cliente.getProfissao(),
            cliente.getEmpresaTrabalho(),
            cliente.getObservacoes(),
            cliente.isAtivo(),
            cliente.getCriadoEm(),
            cliente.getAtualizadoEm()
        );
    }
}
