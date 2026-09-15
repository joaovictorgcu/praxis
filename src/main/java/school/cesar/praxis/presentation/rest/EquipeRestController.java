package school.cesar.praxis.presentation.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.port.in.EquipesUseCases;

import java.util.List;

@RestController
@RequestMapping("/api/equipes")
public class EquipeRestController {

    private final EquipesUseCases.CadastrarEquipe cadastrar;
    private final EquipesUseCases.ListarEquipes listar;
    private final EquipesUseCases.AdicionarMembro adicionarMembro;
    private final EquipesUseCases.RemoverMembro removerMembro;

    public EquipeRestController(EquipesUseCases.CadastrarEquipe cadastrar,
                                EquipesUseCases.ListarEquipes listar,
                                EquipesUseCases.AdicionarMembro adicionarMembro,
                                EquipesUseCases.RemoverMembro removerMembro) {
        this.cadastrar = cadastrar;
        this.listar = listar;
        this.adicionarMembro = adicionarMembro;
        this.removerMembro = removerMembro;
    }

    public record NovaEquipe(String nome) {
    }

    @PostMapping
    public ResponseEntity<EquipesUseCases.ItemEquipe> cadastrar(@RequestBody NovaEquipe corpo) {
        return ResponseEntity.ok(cadastrar.executar(new EquipesUseCases.CadastrarEquipe.Comando(corpo.nome())));
    }

    @GetMapping
    public List<EquipesUseCases.ItemEquipe> listar() {
        return listar.executar();
    }

    @PostMapping("/{id}/membros/{advogadoId}")
    public EquipesUseCases.ItemEquipe adicionarMembro(@PathVariable Long id, @PathVariable Long advogadoId) {
        return adicionarMembro.executar(id, advogadoId);
    }

    @DeleteMapping("/{id}/membros/{advogadoId}")
    public EquipesUseCases.ItemEquipe removerMembro(@PathVariable Long id, @PathVariable Long advogadoId) {
        return removerMembro.executar(id, advogadoId);
    }
}
