package school.cesar.praxis.presentation.rest;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.port.in.ComarcasUseCases;
import school.cesar.praxis.presentation.web.seguranca.SomenteChefe;

@RestController
@RequestMapping("/api/comarcas")
public class ComarcaRestController {

    private final ComarcasUseCases.Cadastrar cadastrar;
    private final ComarcasUseCases.Atualizar atualizar;
    private final ComarcasUseCases.Listar listar;
    private final ComarcasUseCases.BuscarPorId buscar;

    public ComarcaRestController(
        ComarcasUseCases.Cadastrar cadastrar,
        ComarcasUseCases.Atualizar atualizar,
        ComarcasUseCases.Listar listar,
        ComarcasUseCases.BuscarPorId buscar
    ) {
        this.cadastrar = cadastrar;
        this.atualizar = atualizar;
        this.listar = listar;
        this.buscar = buscar;
    }

    public record Cadastro(
        String nome,
        String municipio,
        String uf,
        String tribunal,
        String endereco,
        String telefone,
        String email,
        String horarioAtendimento,
        String observacoes
    ) {}

    @GetMapping
    public List<ComarcasUseCases.ItemComarca> listar() {
        return listar.executar();
    }

    @GetMapping("/{id}")
    public ComarcasUseCases.ItemComarca buscar(@PathVariable Long id) {
        return buscar.executar(id);
    }

    @SomenteChefe
    @PostMapping
    public ResponseEntity<ComarcasUseCases.ItemComarca> cadastrar(@RequestBody Cadastro corpo) {
        return ResponseEntity.ok(
            cadastrar.executar(
                new ComarcasUseCases.Cadastrar.Comando(
                    corpo.nome(),
                    corpo.municipio(),
                    corpo.uf(),
                    corpo.tribunal(),
                    corpo.endereco(),
                    corpo.telefone(),
                    corpo.email(),
                    corpo.horarioAtendimento(),
                    corpo.observacoes()
                )
            )
        );
    }

    @SomenteChefe
    @PutMapping("/{id}")
    public ComarcasUseCases.ItemComarca atualizar(@PathVariable Long id, @RequestBody Cadastro corpo) {
        return atualizar.executar(
            new ComarcasUseCases.Atualizar.Comando(
                id,
                corpo.nome(),
                corpo.municipio(),
                corpo.uf(),
                corpo.tribunal(),
                corpo.endereco(),
                corpo.telefone(),
                corpo.email(),
                corpo.horarioAtendimento(),
                corpo.observacoes()
            )
        );
    }
}
