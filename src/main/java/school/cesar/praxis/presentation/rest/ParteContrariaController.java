package school.cesar.praxis.presentation.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.dto.CriarParteContrariaRequest;
import school.cesar.praxis.application.dto.ParteContrariaResponse;
import school.cesar.praxis.application.port.in.ParteContrariaUseCase;
import school.cesar.praxis.domain.compartilhado.TipoPessoa;

import java.util.List;

@RestController
@RequestMapping("/api/partes-contrarias")
public class ParteContrariaController {

    private final ParteContrariaUseCase parteContrariaUseCase;

    public ParteContrariaController(ParteContrariaUseCase parteContrariaUseCase) {
        this.parteContrariaUseCase = parteContrariaUseCase;
    }

    @PostMapping
    public ResponseEntity<ParteContrariaResponse> criarParteContraria(
            @RequestBody CriarParteContrariaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(parteContrariaUseCase.criarParteContraria(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParteContrariaResponse> consultarParteContraria(@PathVariable Long id) {
        ParteContrariaResponse parteContraria = parteContrariaUseCase.consultarParteContraria(id);
        if (parteContraria == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(parteContraria);
    }

    @GetMapping("/cpf-cnpj/{cpfOuCnpj}")
    public ResponseEntity<ParteContrariaResponse> consultarPorCpfOuCnpj(@PathVariable String cpfOuCnpj) {
        ParteContrariaResponse parteContraria = parteContrariaUseCase.consultarPorCpfOuCnpj(cpfOuCnpj);
        if (parteContraria == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(parteContraria);
    }

    @GetMapping
    public ResponseEntity<List<ParteContrariaResponse>> listarPartesContrarias() {
        return ResponseEntity.ok(parteContrariaUseCase.listarPartesContrarias());
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<ParteContrariaResponse>> listarPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(parteContrariaUseCase.listarPorTipo(TipoPessoa.de(tipo)));
    }

    @GetMapping("/cidade/{cidade}")
    public ResponseEntity<List<ParteContrariaResponse>> listarPorCidade(@PathVariable String cidade) {
        return ResponseEntity.ok(parteContrariaUseCase.listarPorCidade(cidade));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParteContrariaResponse> editarParteContraria(
            @PathVariable Long id,
            @RequestBody CriarParteContrariaRequest request) {
        return ResponseEntity.ok(parteContrariaUseCase.editarParteContraria(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarParteContraria(@PathVariable Long id) {
        parteContrariaUseCase.deletarParteContraria(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reativar")
    public ResponseEntity<Void> reativarParteContraria(@PathVariable Long id) {
        parteContrariaUseCase.reativarParteContraria(id);
        return ResponseEntity.ok().build();
    }
}
