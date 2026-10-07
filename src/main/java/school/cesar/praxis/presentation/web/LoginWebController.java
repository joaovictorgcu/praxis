package school.cesar.praxis.presentation.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import school.cesar.praxis.application.port.in.EscritoriosUseCases;
import school.cesar.praxis.application.port.in.UsuariosUseCases;
import school.cesar.praxis.domain.escritorio.Escritorio;
import school.cesar.praxis.domain.usuario.CredenciaisInvalidasException;
import school.cesar.praxis.domain.usuario.Usuario;
import school.cesar.praxis.presentation.web.seguranca.ProtecaoForcaBruta;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;

/**
 * Entrada e saida do painel. Quem loga e o escritorio (login unico, sem
 * usuario individual dentro dele) ou um usuario avulso (administrador da
 * demonstracao) - nao contem regra: traduz o formulario no caso de uso de
 * autenticacao e guarda na sessao so a projecao {@link UsuarioLogado}.
 */
@Controller
public class LoginWebController {

    private final EscritoriosUseCases.Autenticar autenticarEscritorio;
    private final UsuariosUseCases.Autenticar autenticarUsuario;
    private final ProtecaoForcaBruta protecao;

    public LoginWebController(EscritoriosUseCases.Autenticar autenticarEscritorio,
                              UsuariosUseCases.Autenticar autenticarUsuario,
                              ProtecaoForcaBruta protecao) {
        this.autenticarEscritorio = autenticarEscritorio;
        this.autenticarUsuario = autenticarUsuario;
        this.protecao = protecao;
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String proximo,
                        HttpSession sessao, Model model) {
        if (UsuarioLogado.da(sessao) != null) {
            return "redirect:" + destinoSeguro(proximo);
        }
        model.addAttribute("proximo", destinoSeguro(proximo));
        return "login";
    }

    @PostMapping("/login")
    public String entrar(@RequestParam String email,
                         @RequestParam String senha,
                         @RequestParam(required = false) String proximo,
                         HttpServletRequest requisicao,
                         Model model) {
        model.addAttribute("email", email);
        model.addAttribute("proximo", destinoSeguro(proximo));

        if (protecao.bloqueado(email)) {
            model.addAttribute("erro", "muitas tentativas para este e-mail; aguarde "
                    + protecao.segundosRestantes(email) + " segundos");
            return "login";
        }
        try {
            UsuarioLogado logado = autenticar(email, senha);
            protecao.registrarSucesso(email);
            // Sessao nova apos autenticar: evita fixacao de sessao.
            HttpSession anterior = requisicao.getSession(false);
            if (anterior != null) {
                anterior.invalidate();
            }
            requisicao.getSession(true).setAttribute(UsuarioLogado.CHAVE_SESSAO, logado);
            return "redirect:" + destinoSeguro(proximo);
        } catch (CredenciaisInvalidasException invalidas) {
            protecao.registrarFalha(email);
            model.addAttribute("erro", invalidas.getMessage());
            return "login";
        }
    }

    /** Usuario avulso cobre so o administrador da demonstracao; o caso comum e o escritorio. */
    private UsuarioLogado autenticar(String email, String senha) {
        try {
            Usuario usuario = autenticarUsuario.executar(new UsuariosUseCases.Autenticar.Comando(email, senha));
            return UsuarioLogado.de(usuario);
        } catch (CredenciaisInvalidasException invalidas) {
            Escritorio escritorio = autenticarEscritorio.executar(
                    new EscritoriosUseCases.Autenticar.Comando(email, senha));
            return UsuarioLogado.de(escritorio);
        }
    }

    @PostMapping("/sair")
    public String sair(HttpSession sessao, RedirectAttributes flash) {
        if (sessao != null) {
            sessao.invalidate();
        }
        flash.addFlashAttribute("mensagem", "Sessão encerrada.");
        return "redirect:/login";
    }

    /** So aceita caminho interno do painel: nada de redirecionar para fora. */
    static String destinoSeguro(String proximo) {
        if (proximo == null || !proximo.startsWith("/painel") || proximo.startsWith("//")) {
            return "/painel";
        }
        return proximo;
    }
}
