package school.cesar.praxis.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.port.in.ModelosUseCases;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.domain.modelo.ContextoTexto;

/**
 * Camada de apresentacao web do cadastro de modelos de peca. Nao contem regra -
 * so traduz formulario em caso de uso.
 */
@Controller
@RequestMapping("/painel/modelos")
public class ModeloWebController {

    private final ModelosUseCases.CadastrarModelo cadastrar;
    private final ModelosUseCases.ListarModelos listar;
    private final ModelosUseCases.RemoverModelo remover;

    public ModeloWebController(ModelosUseCases.CadastrarModelo cadastrar,
                               ModelosUseCases.ListarModelos listar,
                               ModelosUseCases.RemoverModelo remover) {
        this.cadastrar = cadastrar;
        this.listar = listar;
        this.remover = remover;
    }

    @GetMapping
    public String modelos(Model model) {
        return montarTela(model);
    }

    @PostMapping
    public String cadastrar(@RequestParam String codigo,
                            @RequestParam String nome,
                            @RequestParam TipoDocumento tipo,
                            @RequestParam(required = false) String titulo,
                            @RequestParam String corpo,
                            @RequestParam String pedidos,
                            @RequestParam(required = false) Boolean enderecaAoJuizo,
                            Model model) {
        try {
            cadastrar.executar(new ModelosUseCases.CadastrarModelo.Comando(
                    codigo, nome, tipo, titulo, corpo, pedidos,
                    Boolean.TRUE.equals(enderecaAoJuizo)));
        } catch (IllegalArgumentException invalido) {
            model.addAttribute("erro", invalido.getMessage());
        }
        return montarTela(model);
    }

    @PostMapping("/{id}/remover")
    public String remover(@PathVariable Long id) {
        remover.executar(id);
        return "redirect:/painel/modelos";
    }

    private String montarTela(Model model) {
        model.addAttribute("modelos", listar.executar());
        model.addAttribute("tipos", TipoDocumento.values());
        model.addAttribute("reservados", ContextoTexto.nomesReservados());
        return "modelos";
    }
}
