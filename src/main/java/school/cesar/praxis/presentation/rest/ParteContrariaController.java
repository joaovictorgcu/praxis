package school.cesar.praxis.presentation.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.dto.CriarParteContrariaRequest;
import school.cesar.praxis.application.dto.ParteContrariaResponse;
import school.cesar.praxis.application.port.in.ParteContrariaUseCase;
import school.cesar.praxis.domain.partecontraria.TipoPessoa;

import java.util.List;

/**
 * Controller REST para Partes Contrárias.
 * Endpoints para CRUD completo de partes contrárias.
 */
@RestController
@RequestMapping("/api/partes-contrarias")
@CrossOrigin(origins = "*")
public class ParteContrariaController {

    private final ParteContrariaUseCase parteContrariaUseCase;

    public ParteContrariaController(ParteContrariaUseCase parteContrariaUseCase) {
        this.parteContrariaUseCase = parteContrariaUseCase;
    }

    /**
     * POST /api/partes-contrarias
     * Criar uma nova parte contrária
     */
    @PostMapping
    public ResponseEntity<?> criarParteContraria(@RequestBody CriarParteContrariaRequest request) {
        try {
            ParteContrariaResponse response = parteContrariaUseCase.criarParteContraria(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new AudienciaController.ErrorResponse("DADOS_INVALIDOS", e.getMessage()));
        }
    }

    /**
     * GET /api/partes-contrarias/{id}
     * Consultar parte contrária por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> consultarParteContraria(@PathVariable Long id) {
        ParteContrariaResponse parteContraria = parteContrariaUseCase.consultarParteContraria(id);
        if (parteContraria == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(parteContraria);
    }

    /**
     * GET /api/partes-contrarias/cpf-cnpj/{cpfOuCnpj}
     * Consultar parte contrária por CPF/CNPJ
     */
    @GetMapping("/cpf-cnpj/{cpfOuCnpj}")
    public ResponseEntity<?> consultarPorCpfOuCnpj(@PathVariable String cpfOuCnpj) {
        ParteContrariaResponse parteContraria = parteContrariaUseCase.consultarPorCpfOuCnpj(cpfOuCnpj);
        if (parteContraria == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(parteContraria);
    }

    /**
     * GET /api/partes-contrarias
     * Listar todas as partes contrárias ativas
     */
    @GetMapping
    public ResponseEntity<List<ParteContrariaResponse>> listarPartesContrarias() {
        List<ParteContrariaResponse> partes = parteContrariaUseCase.listarPartesContrarias();
        return ResponseEntity.ok(partes);
    }

    /**
     * GET /api/partes-contrarias/tipo/{tipo}
     * Listar partes contrárias por tipo (FISICA ou JURIDICA)
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<?> listarPorTipo(@PathVariable String tipo) {
        try {
            TipoPessoa tipoPessoa = TipoPessoa.valueOf(tipo.toUpperCase());
            List<ParteContrariaResponse> partes = parteContrariaUseCase.listarPorTipo(tipoPessoa);
            return ResponseEntity.ok(partes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new AudienciaController.ErrorResponse("TIPO_INVALIDO", 
                    "Tipo deve ser FISICA ou JURIDICA"));
        }
    }

    /**
     * GET /api/partes-contrarias/cidade/{cidade}
     * Listar partes contrárias por cidade
     */
    @GetMapping("/cidade/{cidade}")
    public ResponseEntity<List<ParteContrariaResponse>> listarPorCidade(@PathVariable String cidade) {
        List<ParteContrariaResponse> partes = parteContrariaUseCase.listarPorCidade(cidade);
        return ResponseEntity.ok(partes);
    }

    /**
     * PUT /api/partes-contrarias/{id}
     * Editar uma parte contrária
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> editarParteContraria(
        @PathVariable Long id,
        @RequestBody CriarParteContrariaRequest request
    ) {
        try {
            ParteContrariaResponse response = parteContrariaUseCase.editarParteContraria(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new AudienciaController.ErrorResponse("ERRO", e.getMessage()));
        }
    }

    /**
     * DELETE /api/partes-contrarias/{id}
     * Deletar (desativar) uma parte contrária
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarParteContraria(@PathVariable Long id) {
        try {
            parteContrariaUseCase.deletarParteContraria(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/partes-contrarias/{id}/reativar
     * Reativar uma parte contrária
     */
    @PostMapping("/{id}/reativar")
    public ResponseEntity<?> reativarParteContraria(@PathVariable Long id) {
        try {
            parteContrariaUseCase.reativarParteContraria(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new AudienciaController.ErrorResponse("ERRO", e.getMessage()));
        }
    }
}
