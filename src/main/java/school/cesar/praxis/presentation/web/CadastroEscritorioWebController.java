package school.cesar.praxis.presentation.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import school.cesar.praxis.application.port.in.EscritoriosUseCases;
import school.cesar.praxis.domain.escritorio.Escritorio;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;

/**
 * Cadastro publico de escritorio: unica forma de criar um login no sistema.
 * Ao cadastrar, o escritorio ja entra logado - fica a ele cadastrar depois os
 * proprios advogados dentro do painel.
 */
@Controller
public class CadastroEscritorioWebController {

    private final EscritoriosUseCases.CadastrarEscritorio cadastrar;

    public CadastroEscritorioWebController(EscritoriosUseCases.CadastrarEscritorio cadastrar) {
        this.cadastrar = cadastrar;
    }

    @GetMapping("/cadastro")
    public String cadastro(HttpSession sessao) {
        if (UsuarioLogado.da(sessao) != null) {
            return "redirect:/painel";
        }
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@RequestParam String nome,
                            @RequestParam(required = false) String cnpj,
                            @RequestParam String email,
                            @RequestParam(required = false) String telefone,
                            @RequestParam(required = false) String uf,
                            @RequestParam(required = false) String comarca,
                            @RequestParam String senha,
                            @RequestParam String confirmacao,
                            HttpServletRequest requisicao,
                            Model model) {
        model.addAttribute("nome", nome);
        model.addAttribute("cnpj", cnpj);
        model.addAttribute("email", email);
        model.addAttribute("telefone", telefone);
        model.addAttribute("uf", uf);
        model.addAttribute("comarca", comarca);

        if (!senha.equals(confirmacao)) {
            model.addAttribute("erro", "a confirmacao nao coincide com a senha");
            return "cadastro";
        }
        try {
            Escritorio escritorio = cadastrar.executar(new EscritoriosUseCases.CadastrarEscritorio.Comando(
                    nome, cnpj, email, telefone, uf, comarca, senha));
            requisicao.getSession(true).setAttribute(UsuarioLogado.CHAVE_SESSAO, UsuarioLogado.de(escritorio));
            return "redirect:/painel";
        } catch (IllegalArgumentException falha) {
            model.addAttribute("erro", falha.getMessage());
            return "cadastro";
        }
    }
}
