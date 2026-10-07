package school.cesar.praxis.presentation.web;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import school.cesar.praxis.application.port.in.AdvogadosUseCases;
import school.cesar.praxis.presentation.web.seguranca.SomenteChefe;

@Controller
@RequestMapping("/painel/advogados")
public class AdvogadoWebController {

    private final AdvogadosUseCases.ListarAdvogados listar;
    private final AdvogadosUseCases.PesquisarAdvogados pesquisar;
    private final AdvogadosUseCases.BuscarAdvogadoPorId buscarPorId;
    private final AdvogadosUseCases.CadastrarAdvogado cadastrar;
    private final AdvogadosUseCases.AtualizarAdvogado atualizar;
    private final AdvogadosUseCases.AtivarAdvogado ativar;
    private final AdvogadosUseCases.DesativarAdvogado desativar;

    public AdvogadoWebController(
        AdvogadosUseCases.ListarAdvogados listar,
        AdvogadosUseCases.PesquisarAdvogados pesquisar,
        AdvogadosUseCases.BuscarAdvogadoPorId buscarPorId,
        AdvogadosUseCases.CadastrarAdvogado cadastrar,
        AdvogadosUseCases.AtualizarAdvogado atualizar,
        AdvogadosUseCases.AtivarAdvogado ativar,
        AdvogadosUseCases.DesativarAdvogado desativar
    ) {
        this.listar = listar;
        this.pesquisar = pesquisar;
        this.buscarPorId = buscarPorId;
        this.cadastrar = cadastrar;
        this.atualizar = atualizar;
        this.ativar = ativar;
        this.desativar = desativar;
    }

    @GetMapping
    public String advogados(
        @RequestParam(required = false) String busca,
        @RequestParam(required = false) Long editar,
        Model model
    ) {
        String filtro = busca == null ? "" : busca.trim();
        model.addAttribute("busca", busca);
        model.addAttribute("advogados", filtro.isEmpty() ? listar.executar() : pesquisar.executar(filtro));

        if (editar != null) {
            try {
                model.addAttribute("editando", buscarPorId.executar(editar));
            } catch (NoSuchElementException ignorado) {}
        }
        return "advogados";
    }

    @SomenteChefe
    @PostMapping
    public String cadastrar(
        @RequestParam String nome,
        @RequestParam String email,
        @RequestParam String oab,
        @RequestParam(required = false) String telefone,
        @RequestParam(required = false) String especialidade,
        @RequestParam(defaultValue = "false") boolean disponivel,
        RedirectAttributes flash
    ) {
        try {
            AdvogadosUseCases.ItemAdvogado novo = cadastrar.executar(
                new AdvogadosUseCases.CadastrarAdvogado.Comando(
                    nome,
                    email,
                    oab,
                    telefone,
                    especialidade,
                    disponivel
                )
            );
            flash.addFlashAttribute(
                "mensagem",
                "Advogado " + novo.nome() + " cadastrado com a OAB " + novo.oab() + "."
            );
        } catch (IllegalArgumentException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/advogados";
    }

    @SomenteChefe
    @PostMapping("/{id}/atualizar")
    public String atualizar(
        @PathVariable Long id,
        @RequestParam String nome,
        @RequestParam String email,
        @RequestParam(required = false) String telefone,
        @RequestParam(required = false) String especialidade,
        @RequestParam(defaultValue = "false") boolean disponivel,
        RedirectAttributes flash
    ) {
        try {
            AdvogadosUseCases.ItemAdvogado salvo = atualizar.executar(
                new AdvogadosUseCases.AtualizarAdvogado.Comando(
                    id,
                    nome,
                    email,
                    telefone,
                    especialidade,
                    disponivel
                )
            );
            flash.addFlashAttribute("mensagem", "Advogado " + salvo.nome() + " atualizado.");
        } catch (IllegalArgumentException | NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/advogados";
    }

    @SomenteChefe
    @PostMapping("/{id}/ativar")
    public String ativar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            AdvogadosUseCases.ItemAdvogado advogado = ativar.executar(id);
            flash.addFlashAttribute("mensagem", "Advogado " + advogado.nome() + " ativado.");
        } catch (NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/advogados";
    }

    @SomenteChefe
    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            AdvogadosUseCases.ItemAdvogado advogado = desativar.executar(id);
            flash.addFlashAttribute("mensagem", "Advogado " + advogado.nome() + " desativado.");
        } catch (NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/advogados";
    }
}
