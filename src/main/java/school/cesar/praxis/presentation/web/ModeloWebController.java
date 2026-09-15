package school.cesar.praxis.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import school.cesar.praxis.application.port.in.ModelosUseCases;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.domain.modelo.ContextoTexto;
import school.cesar.praxis.presentation.web.seguranca.SomenteChefe;

import java.util.NoSuchElementException;

/**
 * Camada de apresentacao web do cadastro de modelos de peca. Nao contem regra -
 * so traduz formulario em caso de uso.
 *
 * <p>Remover modelo tira uma peca do alcance de todo o escritorio, entao e
 * acao do chefe.
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
                            RedirectAttributes flash) {
        try {
            cadastrar.executar(new ModelosUseCases.CadastrarModelo.Comando(
                    codigo, nome, tipo, titulo, corpo, pedidos,
                    Boolean.TRUE.equals(enderecaAoJuizo)));
            flash.addFlashAttribute("mensagem", "Modelo " + codigo + " cadastrado.");
        } catch (IllegalArgumentException invalido) {
            flash.addFlashAttribute("erro", invalido.getMessage());
        }
        return "redirect:/painel/modelos";
    }

    @SomenteChefe
    @PostMapping("/{id}/remover")
    public String remover(@PathVariable Long id, RedirectAttributes flash) {
        try {
            remover.executar(id);
            flash.addFlashAttribute("mensagem", "Modelo removido.");
        } catch (IllegalArgumentException | NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/modelos";
    }

    private String montarTela(Model model) {
        model.addAttribute("modelos", listar.executar());
        model.addAttribute("tipos", TipoDocumento.values());
        model.addAttribute("reservados", ContextoTexto.nomesReservados());
        return "modelos";
    }
}
