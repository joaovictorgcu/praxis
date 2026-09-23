package school.cesar.praxis.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import school.cesar.praxis.application.port.in.FeriadosUseCases;
import school.cesar.praxis.domain.feriado.Abrangencia;
import school.cesar.praxis.domain.feriado.Jurisdicao;
import school.cesar.praxis.presentation.web.seguranca.SomenteChefe;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

/**
 * Camada de apresentacao web do cadastro de feriados. Controller proprio para
 * nao inflar o painel de prazos. Nao contem regra - so traduz formulario em
 * caso de uso.
 *
 * <p>Cadastrar e remover feriado mudam a contagem de prazo de todo o
 * escritorio, entao sao acoes do chefe. A consulta fica aberta a qualquer
 * usuario logado: o advogado precisa ver o calendario que rege os prazos dele.
 */
@Controller
@RequestMapping("/painel/feriados")
public class FeriadoWebController {

    private final FeriadosUseCases.CadastrarFeriado cadastrar;
    private final FeriadosUseCases.ListarFeriados listar;
    private final FeriadosUseCases.RemoverFeriado remover;
    private final FeriadosUseCases.ConsultarDiaUtil consultarDiaUtil;
    private final Jurisdicao foro;

    public FeriadoWebController(FeriadosUseCases.CadastrarFeriado cadastrar,
                                FeriadosUseCases.ListarFeriados listar,
                                FeriadosUseCases.RemoverFeriado remover,
                                FeriadosUseCases.ConsultarDiaUtil consultarDiaUtil,
                                Jurisdicao foro) {
        this.cadastrar = cadastrar;
        this.listar = listar;
        this.remover = remover;
        this.consultarDiaUtil = consultarDiaUtil;
        this.foro = foro;
    }

    @GetMapping
    public String feriados(@RequestParam(required = false) String verificar, Model model) {
        if (verificar != null && !verificar.isBlank()) {
            model.addAttribute("consulta", consultarDiaUtil.executar(LocalDate.parse(verificar)));
        }
        return montarTela(model);
    }

    @SomenteChefe
    @PostMapping
    public String cadastrar(@RequestParam String descricao,
                            @RequestParam String data,
                            @RequestParam(required = false) Boolean repeteTodoAno,
                            @RequestParam Abrangencia.Nivel nivel,
                            @RequestParam(required = false) String abrangencia,
                            RedirectAttributes flash) {
        try {
            cadastrar.executar(new FeriadosUseCases.CadastrarFeriado.Comando(
                    descricao,
                    LocalDate.parse(data),
                    Boolean.TRUE.equals(repeteTodoAno),
                    nivel,
                    abrangencia));
            flash.addFlashAttribute("mensagem", "Feriado \"" + descricao + "\" cadastrado.");
        } catch (IllegalArgumentException | DateTimeParseException invalido) {
            flash.addFlashAttribute("erro", invalido.getMessage());
        }
        return "redirect:/painel/feriados";
    }

    @SomenteChefe
    @PostMapping("/{id}/remover")
    public String remover(@PathVariable Long id, RedirectAttributes flash) {
        try {
            remover.executar(id);
            flash.addFlashAttribute("mensagem", "Feriado removido do calendário.");
        } catch (IllegalArgumentException | NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/feriados";
    }

    private String montarTela(Model model) {
        model.addAttribute("feriados", listar.executar());
        model.addAttribute("niveis", Abrangencia.Nivel.values());
        model.addAttribute("foro", foro);
        return "feriados";
    }
}
