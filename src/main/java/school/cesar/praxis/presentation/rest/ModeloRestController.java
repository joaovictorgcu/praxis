package school.cesar.praxis.presentation.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.port.in.ModelosUseCases;
import school.cesar.praxis.domain.documento.TipoDocumento;

import java.util.List;

/** Camada de apresentacao (REST) da funcionalidade Cadastro de modelos. */
@RestController
@RequestMapping("/api/modelos")
public class ModeloRestController {

    private final ModelosUseCases.CadastrarModelo cadastrar;
    private final ModelosUseCases.ListarModelos listar;
    private final ModelosUseCases.RemoverModelo remover;

    public ModeloRestController(ModelosUseCases.CadastrarModelo cadastrar,
                                ModelosUseCases.ListarModelos listar,
                                ModelosUseCases.RemoverModelo remover) {
        this.cadastrar = cadastrar;
        this.listar = listar;
        this.remover = remover;
    }

    public record NovoModelo(String codigo,
                             String nome,
                             TipoDocumento tipo,
                             String titulo,
                             String corpo,
                             String pedidos,
                             boolean enderecaAoJuizo) {
    }

    @PostMapping
    public ModelosUseCases.ItemModelo cadastrar(@RequestBody NovoModelo corpo) {
        return cadastrar.executar(new ModelosUseCases.CadastrarModelo.Comando(
                corpo.codigo(),
                corpo.nome(),
                corpo.tipo(),
                corpo.titulo(),
                corpo.corpo(),
                corpo.pedidos(),
                corpo.enderecaAoJuizo()));
    }

    @GetMapping
    public List<ModelosUseCases.ItemModelo> listar() {
        return listar.executar();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        remover.executar(id);
        return ResponseEntity.noContent().build();
    }
}
