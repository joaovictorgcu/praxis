package school.cesar.praxis.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Ficha do prazo ({@code /painel/prazos/{id}}) e as acoes do ciclo de vida:
 * suspender, retomar, dilatar, cumprir, cancelar e reabrir. Nao contem regra -
 * traduz formulario em caso de uso; o dominio decide se a acao cabe no estado
 * atual do prazo e o caso de uso grava o evento na trilha.
 */
@Controller
@RequestMapping("/painel/prazos")
public class PrazoWebController {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PrazosUseCases.ConsultarPrazo consultar;
    private final PrazosUseCases.SuspenderPrazo suspender;
    private final PrazosUseCases.RetomarPrazo retomar;
    private final PrazosUseCases.DilatarPrazo dilatar;
    private final PrazosUseCases.CumprirPrazo cumprir;
    private final PrazosUseCases.CancelarPrazo cancelar;
    private final PrazosUseCases.ReabrirPrazo reabrir;

    public PrazoWebController(PrazosUseCases.ConsultarPrazo consultar,
                              PrazosUseCases.SuspenderPrazo suspender,
                              PrazosUseCases.RetomarPrazo retomar,
                              PrazosUseCases.DilatarPrazo dilatar,
                              PrazosUseCases.CumprirPrazo cumprir,
                              PrazosUseCases.CancelarPrazo cancelar,
                              PrazosUseCases.ReabrirPrazo reabrir) {
        this.consultar = consultar;
        this.suspender = suspender;
        this.retomar = retomar;
        this.dilatar = dilatar;
        this.cumprir = cumprir;
        this.cancelar = cancelar;
        this.reabrir = reabrir;
    }

    @GetMapping("/{id}")
    public String ficha(@PathVariable Long id, UsuarioLogado usuario, Model model, RedirectAttributes flash) {
        try {
            PrazosUseCases.ConsultarPrazo.DetalhePrazo prazo = consultar.executar(id);
            model.addAttribute("prazo", prazo);
            model.addAttribute("hoje", LocalDate.now());
            model.addAttribute("podeEncerrar", usuario.chefe()
                    || (usuario.oab() != null && usuario.oab().equalsIgnoreCase(prazo.responsavelOab())));
            return "prazo";
        } catch (NoSuchElementException | IllegalArgumentException ausente) {
            flash.addFlashAttribute("erro", ausente.getMessage());
            return "redirect:/painel";
        }
    }

    @PostMapping("/{id}/suspender")
    public String suspender(@PathVariable Long id,
                            @RequestParam String inicio,
                            @RequestParam String motivo,
                            UsuarioLogado usuario,
                            RedirectAttributes flash) {
        return executar(flash, destino(id, null),
                prazo -> "Prazo suspenso desde " + DATA.format(LocalDate.parse(inicio))
                        + ": o saldo fica congelado até a retomada.",
                () -> suspender.executar(new PrazosUseCases.SuspenderPrazo.Comando(
                        id, LocalDate.parse(inicio), motivo, autor(usuario))));
    }

    @PostMapping("/{id}/retomar")
    public String retomar(@PathVariable Long id,
                          @RequestParam String retomada,
                          UsuarioLogado usuario,
                          RedirectAttributes flash) {
        return executar(flash, destino(id, null),
                prazo -> "Prazo retomado: o saldo voltou a correr e o novo vencimento é "
                        + DATA.format(prazo.getVencimento()) + ".",
                () -> retomar.executar(new PrazosUseCases.RetomarPrazo.Comando(
                        id, LocalDate.parse(retomada), autor(usuario))));
    }

    @PostMapping("/{id}/dilatar")
    public String dilatar(@PathVariable Long id,
                          @RequestParam int dias,
                          @RequestParam String fundamento,
                          UsuarioLogado usuario,
                          RedirectAttributes flash) {
        return executar(flash, destino(id, null),
                prazo -> "Prazo dilatado: o novo vencimento é " + DATA.format(prazo.getVencimento()) + ".",
                () -> dilatar.executar(new PrazosUseCases.DilatarPrazo.Comando(id, dias, fundamento, autor(usuario))));
    }

    /** Tambem usado pela agenda e pela ficha do processo, que mandam {@code voltar} para retornar a elas. */
    @PostMapping("/{id}/cumprir")
    public String cumprir(@PathVariable Long id,
                          @RequestParam(required = false) String voltar,
                          UsuarioLogado usuario,
                          RedirectAttributes flash) {
        return executar(flash, destino(id, voltar), prazo -> "Prazo registrado como cumprido.",
                () -> cumprir.executar(id, autor(usuario)));
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id,
                           @RequestParam String motivo,
                           UsuarioLogado usuario,
                           RedirectAttributes flash) {
        return executar(flash, destino(id, null), prazo -> "Prazo cancelado: saiu da agenda e da varredura.",
                () -> cancelar.executar(new PrazosUseCases.CancelarPrazo.Comando(id, motivo, autor(usuario))));
    }

    @PostMapping("/{id}/reabrir")
    public String reabrir(@PathVariable Long id,
                          @RequestParam String motivo,
                          UsuarioLogado usuario,
                          RedirectAttributes flash) {
        return executar(flash, destino(id, null),
                prazo -> "Prazo reaberto: volta a " + prazo.getSituacao().rotulo() + ".",
                () -> reabrir.executar(new PrazosUseCases.ReabrirPrazo.Comando(id, motivo, autor(usuario))));
    }

    // --- Apoio ---

    static PrazosUseCases.Autor autor(UsuarioLogado usuario) {
        return usuario == null
                ? PrazosUseCases.Autor.sistema()
                : new PrazosUseCases.Autor(usuario.oab(), usuario.nome(), usuario.chefe());
    }

    /** Volta para a tela que chamou, desde que seja do painel; senao, para a ficha do prazo. */
    private static String destino(Long id, String voltar) {
        if (voltar != null && voltar.startsWith("/painel") && !voltar.startsWith("//") && !voltar.contains("\\")) {
            return voltar;
        }
        return "/painel/prazos/" + id;
    }

    /**
     * POST-redirect-GET: a acao roda, o resultado vai como flash e a tela e
     * recarregada por GET. Falha de dominio vira mensagem na pagina, nunca 500.
     */
    private static String executar(RedirectAttributes flash, String destino,
                                   Function<Prazo, String> sucesso, Supplier<Prazo> acao) {
        try {
            Prazo prazo = acao.get();
            flash.addFlashAttribute("mensagem", sucesso.apply(prazo));
        } catch (DateTimeParseException data) {
            flash.addFlashAttribute("erro", "data inválida: " + data.getParsedString());
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:" + destino;
    }
}
