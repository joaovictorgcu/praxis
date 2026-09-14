package school.cesar.praxis.bdd;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import school.cesar.praxis.application.port.in.UsuariosUseCases;
import school.cesar.praxis.application.port.out.UsuarioRepositorio;
import school.cesar.praxis.domain.usuario.CredenciaisInvalidasException;
import school.cesar.praxis.domain.usuario.Papel;
import school.cesar.praxis.domain.usuario.Usuario;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Steps do acesso ao painel. Exercitam os casos de uso reais de autenticacao e
 * cadastro contra o banco; a tela de login e coberta por {@code LoginHttpTest}.
 */
public class AcessoSteps {

    @Autowired
    private UsuariosUseCases.Autenticar autenticar;
    @Autowired
    private UsuariosUseCases.CadastrarUsuario cadastrar;
    @Autowired
    private UsuarioRepositorio usuarios;

    private Usuario logado;
    private CredenciaisInvalidasException recusa;

    @Dado("o usuario {string} com e-mail {string}, OAB {string}, papel {string} e senha {string}")
    public void oUsuario(String nome, String email, String oab, String papel, String senha) {
        if (usuarios.porEmail(email).isEmpty()) {
            cadastrar.executar(new UsuariosUseCases.CadastrarUsuario.Comando(
                    nome, email, oab, Papel.valueOf(papel), senha));
        }
    }

    @Quando("eu entro com e-mail {string} e senha {string}")
    public void euEntro(String email, String senha) {
        logado = null;
        recusa = null;
        try {
            logado = autenticar.executar(new UsuariosUseCases.Autenticar.Comando(email, senha));
        } catch (CredenciaisInvalidasException invalidas) {
            recusa = invalidas;
        }
    }

    @Entao("o acesso deve ser concedido para a OAB {string}")
    public void acessoConcedido(String oab) {
        assertNull(recusa, "acesso foi recusado");
        assertNotNull(logado);
        assertEquals(oab, logado.getOab());
    }

    @E("o usuario logado deve poder aprovar pecas")
    public void podeAprovar() {
        assertTrue(logado.getPapel().podeAprovarPeca());
        assertTrue(logado.isChefe());
    }

    @E("o usuario logado nao deve poder aprovar pecas")
    public void naoPodeAprovar() {
        assertFalse(logado.getPapel().podeAprovarPeca());
        assertFalse(logado.isChefe());
    }

    @Entao("o acesso deve ser negado com {string}")
    public void acessoNegado(String mensagem) {
        assertNull(logado, "acesso deveria ter sido negado");
        assertNotNull(recusa);
        assertEquals(mensagem, recusa.getMessage());
    }

    @Entao("a senha guardada para {string} nao deve ser {string}")
    public void senhaNaoEmTexto(String email, String senha) {
        Usuario guardado = usuarios.porEmail(email).orElseThrow();
        assertNotEquals(senha, guardado.getSenhaCodificada());
        assertFalse(guardado.getSenhaCodificada().contains(senha));
    }

    @Entao("cadastrar outro usuario com e-mail {string} e OAB {string} deve falhar")
    public void cadastroDuplicadoFalha(String email, String oab) {
        assertThrows(IllegalArgumentException.class, () ->
                cadastrar.executar(new UsuariosUseCases.CadastrarUsuario.Comando(
                        "Duplicado", email, oab, Papel.ADVOGADO, "senha123")));
    }
}
