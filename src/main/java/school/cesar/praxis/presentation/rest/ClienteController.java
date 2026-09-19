package school.cesar.praxis.presentation.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.dto.ClienteResponse;
import school.cesar.praxis.application.dto.CriarClienteRequest;
import school.cesar.praxis.application.port.in.ClienteUseCase;
import school.cesar.praxis.domain.compartilhado.TipoPessoa;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteUseCase clienteUseCase;

    public ClienteController(ClienteUseCase clienteUseCase) {
        this.clienteUseCase = clienteUseCase;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> criarCliente(@RequestBody CriarClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteUseCase.criarCliente(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> consultarCliente(@PathVariable Long id) {
        ClienteResponse cliente = clienteUseCase.consultarCliente(id);
        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cliente);
    }

    @GetMapping("/cpf-cnpj/{cpfOuCnpj}")
    public ResponseEntity<ClienteResponse> consultarPorCpfOuCnpj(@PathVariable String cpfOuCnpj) {
        ClienteResponse cliente = clienteUseCase.consultarPorCpfOuCnpj(cpfOuCnpj);
        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cliente);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarClientes() {
        return ResponseEntity.ok(clienteUseCase.listarClientes());
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<ClienteResponse>> listarPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(clienteUseCase.listarPorTipo(TipoPessoa.de(tipo)));
    }

    @GetMapping("/cidade/{cidade}")
    public ResponseEntity<List<ClienteResponse>> listarPorCidade(@PathVariable String cidade) {
        return ResponseEntity.ok(clienteUseCase.listarPorCidade(cidade));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ClienteResponse>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(clienteUseCase.listarPorEstado(estado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> editarCliente(
            @PathVariable Long id,
            @RequestBody CriarClienteRequest request) {
        return ResponseEntity.ok(clienteUseCase.editarCliente(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCliente(@PathVariable Long id) {
        clienteUseCase.deletarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reativar")
    public ResponseEntity<Void> reativarCliente(@PathVariable Long id) {
        clienteUseCase.reativarCliente(id);
        return ResponseEntity.ok().build();
    }
}
