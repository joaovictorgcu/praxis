package school.cesar.praxis.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.port.in.FeriadosUseCases;
import school.cesar.praxis.domain.feriado.Abrangencia;
import school.cesar.praxis.domain.feriado.Jurisdicao;

import java.time.LocalDate;

/**
 * Camada de apresentacao web do cadastro de feriados. Controller proprio para
 * nao inflar o painel de prazos. Nao contem regra - so traduz formulario em
 * caso de uso.
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

    @PostMapping
    public String cadastrar(@RequestParam String descricao,
                            @RequestParam String data,
                            @RequestParam(required = false) Boolean repeteTodoAno,
                            @RequestParam Abrangencia.Nivel nivel,
                            @RequestParam(required = false) String abrangencia,
                            Model model) {
        try {
            cadastrar.executar(new FeriadosUseCases.CadastrarFeriado.Comando(
                    descricao,
                    LocalDate.parse(data),
                    Boolean.TRUE.equals(repeteTodoAno),
                    nivel,
                    abrangencia));
        } catch (IllegalArgumentException invalido) {
            model.addAttribute("erro", invalido.getMessage());
        }
        return montarTela(model);
    }

    @PostMapping("/{id}/remover")
    public String remover(@PathVariable Long id) {
        remover.executar(id);
        return "redirect:/painel/feriados";
    }

    private String montarTela(Model model) {
        model.addAttribute("feriados", listar.executar());
        model.addAttribute("niveis", Abrangencia.Nivel.values());
        model.addAttribute("foro", foro);
        return "feriados";
    }
}
