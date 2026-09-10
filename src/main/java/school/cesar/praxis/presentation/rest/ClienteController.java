package school.cesar.praxis.presentation.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.dto.ClienteResponse;
import school.cesar.praxis.application.dto.CriarClienteRequest;
import school.cesar.praxis.application.port.in.ClienteUseCase;
import school.cesar.praxis.domain.cliente.TipoPessoa;

import java.util.List;

/**
 * Controller REST para Clientes.
 * Endpoints para CRUD completo de clientes.
 */
@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    private final ClienteUseCase clienteUseCase;

    public ClienteController(ClienteUseCase clienteUseCase) {
        this.clienteUseCase = clienteUseCase;
    }

    /**
     * POST /api/clientes
     * Criar um novo cliente
     */
    @PostMapping
    public ResponseEntity<?> criarCliente(@RequestBody CriarClienteRequest request) {
        try {
            ClienteResponse response = clienteUseCase.criarCliente(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new AudienciaController.ErrorResponse("DADOS_INVALIDOS", e.getMessage()));
        }
    }

    /**
     * GET /api/clientes/{id}
     * Consultar cliente por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> consultarCliente(@PathVariable Long id) {
        ClienteResponse cliente = clienteUseCase.consultarCliente(id);
        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cliente);
    }

    /**
     * GET /api/clientes/cpf-cnpj/{cpfOuCnpj}
     * Consultar cliente por CPF/CNPJ
     */
    @GetMapping("/cpf-cnpj/{cpfOuCnpj}")
    public ResponseEntity<?> consultarPorCpfOuCnpj(@PathVariable String cpfOuCnpj) {
        ClienteResponse cliente = clienteUseCase.consultarPorCpfOuCnpj(cpfOuCnpj);
        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cliente);
    }

    /**
     * GET /api/clientes
     * Listar todos os clientes ativos
     */
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarClientes() {
        List<ClienteResponse> clientes = clienteUseCase.listarClientes();
        return ResponseEntity.ok(clientes);
    }

    /**
     * GET /api/clientes/tipo/{tipo}
     * Listar clientes por tipo (FISICA ou JURIDICA)
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<?> listarPorTipo(@PathVariable String tipo) {
        try {
            TipoPessoa tipoPessoa = TipoPessoa.valueOf(tipo.toUpperCase());
            List<ClienteResponse> clientes = clienteUseCase.listarPorTipo(tipoPessoa);
            return ResponseEntity.ok(clientes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new AudienciaController.ErrorResponse("TIPO_INVALIDO", 
                    "Tipo deve ser FISICA ou JURIDICA"));
        }
    }

    /**
     * GET /api/clientes/cidade/{cidade}
     * Listar clientes por cidade
     */
    @GetMapping("/cidade/{cidade}")
    public ResponseEntity<List<ClienteResponse>> listarPorCidade(@PathVariable String cidade) {
        List<ClienteResponse> clientes = clienteUseCase.listarPorCidade(cidade);
        return ResponseEntity.ok(clientes);
    }

    /**
     * GET /api/clientes/estado/{estado}
     * Listar clientes por estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ClienteResponse>> listarPorEstado(@PathVariable String estado) {
        List<ClienteResponse> clientes = clienteUseCase.listarPorEstado(estado);
        return ResponseEntity.ok(clientes);
    }

    /**
     * PUT /api/clientes/{id}
     * Editar um cliente
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> editarCliente(
        @PathVariable Long id,
        @RequestBody CriarClienteRequest request
    ) {
        try {
            ClienteResponse response = clienteUseCase.editarCliente(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new AudienciaController.ErrorResponse("ERRO", e.getMessage()));
        }
    }

    /**
     * DELETE /api/clientes/{id}
     * Deletar (desativar) um cliente
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarCliente(@PathVariable Long id) {
        try {
            clienteUseCase.deletarCliente(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/clientes/{id}/reativar
     * Reativar um cliente
     */
    @PostMapping("/{id}/reativar")
    public ResponseEntity<?> reativarCliente(@PathVariable Long id) {
        try {
            clienteUseCase.reativarCliente(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new AudienciaController.ErrorResponse("ERRO", e.getMessage()));
        }
    }
}
