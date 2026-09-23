package school.cesar.praxis.presentation.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import school.cesar.praxis.application.port.in.UsuariosUseCases;
import school.cesar.praxis.domain.usuario.Papel;
import school.cesar.praxis.domain.usuario.Usuario;
import school.cesar.praxis.presentation.web.seguranca.SomenteChefe;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;

import java.util.NoSuchElementException;

/**
 * Gestao de acesso pela tela: o chefe cadastra e remove usuarios em
 * {@code /painel/usuarios}; qualquer usuario troca a propria senha em
 * {@code /painel/conta}. Nao contem regra - so traduz formulario em caso de uso.
 */
@Controller
@RequestMapping("/painel")
public class UsuarioWebController {

    private final UsuariosUseCases.ListarUsuarios listar;
    private final UsuariosUseCases.CadastrarUsuario cadastrar;
    private final UsuariosUseCases.RemoverUsuario remover;
    private final UsuariosUseCases.TrocarSenha trocarSenha;

    public UsuarioWebController(UsuariosUseCases.ListarUsuarios listar,
                                UsuariosUseCases.CadastrarUsuario cadastrar,
                                UsuariosUseCases.RemoverUsuario remover,
                                UsuariosUseCases.TrocarSenha trocarSenha) {
        this.listar = listar;
        this.cadastrar = cadastrar;
        this.remover = remover;
        this.trocarSenha = trocarSenha;
    }

    // --- Usuarios (chefe) ---

    @SomenteChefe
    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        model.addAttribute("usuarios", listar.executar());
        model.addAttribute("papeis", Papel.values());
        return "usuarios";
    }

    @SomenteChefe
    @PostMapping("/usuarios")
    public String cadastrar(@RequestParam String nome,
                            @RequestParam String email,
                            @RequestParam String oab,
                            @RequestParam Papel papel,
                            @RequestParam String senha,
                            RedirectAttributes flash) {
        try {
            Usuario novo = cadastrar.executar(new UsuariosUseCases.CadastrarUsuario.Comando(
                    nome, email, oab, papel, senha));
            flash.addFlashAttribute("mensagem", novo.getPapel().rotulo() + " " + novo.getNome()
                    + " cadastrado(a) com a OAB " + novo.getOab() + ".");
        } catch (IllegalArgumentException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/usuarios";
    }

    @SomenteChefe
    @PostMapping("/usuarios/{id}/remover")
    public String remover(@PathVariable Long id, UsuarioLogado usuario, RedirectAttributes flash) {
        try {
            remover.executar(new UsuariosUseCases.RemoverUsuario.Comando(id, usuario.id()));
            flash.addFlashAttribute("mensagem", "Usuário removido.");
        } catch (IllegalArgumentException | NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
        }
        return "redirect:/painel/usuarios";
    }

    // --- Conta (qualquer usuario) ---

    @GetMapping("/conta")
    public String conta() {
        return "conta";
    }

    @PostMapping("/conta/senha")
    public String trocarSenha(@RequestParam String senhaAtual,
                              @RequestParam String novaSenha,
                              @RequestParam String confirmacao,
                              UsuarioLogado usuario,
                              HttpSession sessao,
                              RedirectAttributes flash) {
        if (!novaSenha.equals(confirmacao)) {
            flash.addFlashAttribute("erro", "a confirmação não coincide com a nova senha");
            return "redirect:/painel/conta";
        }
        try {
            trocarSenha.executar(new UsuariosUseCases.TrocarSenha.Comando(usuario.id(), senhaAtual, novaSenha));
            flash.addFlashAttribute("mensagem", "Senha alterada. Entre de novo com a nova senha.");
            sessao.invalidate();
            return "redirect:/login";
        } catch (IllegalArgumentException | NoSuchElementException falha) {
            flash.addFlashAttribute("erro", falha.getMessage());
            return "redirect:/painel/conta";
        }
    }
}
