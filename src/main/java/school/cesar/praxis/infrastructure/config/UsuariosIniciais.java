package school.cesar.praxis.infrastructure.config;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import school.cesar.praxis.application.port.in.UsuariosUseCases;
import school.cesar.praxis.application.port.out.UsuarioRepositorio;
import school.cesar.praxis.domain.usuario.CodificadorDeSenha;
import school.cesar.praxis.domain.usuario.Papel;
import school.cesar.praxis.domain.usuario.Usuario;

/**
 * Usuarios de referencia para o sistema nascer acessivel: um administrador
 * (chefe) de login curto, um chefe e os dois advogados responsaveis pelos
 * processos de exemplo (mesmas OABs de {@link DadosDeExemplo}, para o segredo
 * de justica ser demonstravel logado).
 *
 * <p>Roda antes de o servidor abrir a porta (ver {@code afterSingletonsInstantiated})
 * porque sem usuario ninguem entra no painel - inclusive nos testes HTTP.
 */
@Component
@ConditionalOnProperty(name = "praxis.usuarios-iniciais", havingValue = "true",
        matchIfMissing = true)
public class UsuariosIniciais implements SmartInitializingSingleton {

    private final UsuariosUseCases.CadastrarUsuario cadastrar;
    private final UsuarioRepositorio usuarios;
    private final CodificadorDeSenha codificador;
    private final String senhaInicial;
    private final boolean exigirTroca;
    private final String adminUsuario;
    private final String adminSenha;
    private final String dominioEmail;

    public UsuariosIniciais(UsuariosUseCases.CadastrarUsuario cadastrar,
                            UsuarioRepositorio usuarios,
                            CodificadorDeSenha codificador,
                            @Value("${praxis.senha-inicial:praxis123}") String senhaInicial,
                            @Value("${praxis.exigir-troca-senha-inicial:false}") boolean exigirTroca,
                            @Value("${praxis.admin.usuario:admin}") String adminUsuario,
                            @Value("${praxis.admin.senha:}") String adminSenha,
                            @Value("${praxis.dominio-email:praxis.adv.br}") String dominioEmail) {
        this.cadastrar = cadastrar;
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.senhaInicial = senhaInicial;
        this.exigirTroca = exigirTroca;
        this.adminUsuario = adminUsuario;
        this.adminSenha = adminSenha;
        this.dominioEmail = dominioEmail;
    }

    /**
     * Roda depois de todos os singletons existirem e ANTES de o servidor web abrir a
     * porta ({@code ContextRefreshedEvent} dispara depois do servidor subir: um login no
     * primeiro segundo encontraria a tabela vazia). Roda tambem no @SpringBootTest.
     */
    @Override
    public void afterSingletonsInstantiated() {
        carregar();
    }

    public void carregar() {
        boolean vazio = usuarios.listar().isEmpty();
        carregarAdministrador();
        if (!vazio) {
            return;
        }
        // Em dev a senha padrao e conhecida e nao exige troca (demonstracao); em prod
        // (praxis.exigir-troca-senha-inicial=true) o primeiro acesso cai na troca de senha.
        cadastrar.executar(new UsuariosUseCases.CadastrarUsuario.Comando(
                "Carla Mendes", "carla.mendes@" + dominioEmail, "PE00001", Papel.CHEFE, senhaInicial, exigirTroca));
        cadastrar.executar(new UsuariosUseCases.CadastrarUsuario.Comando(
                "Ana Beatriz Souza", "ana.souza@" + dominioEmail, "PE12345", Papel.ADVOGADO, senhaInicial, exigirTroca));
        cadastrar.executar(new UsuariosUseCases.CadastrarUsuario.Comando(
                "Bruno Carvalho", "bruno.carvalho@" + dominioEmail, "PE54321", Papel.ADVOGADO, senhaInicial, exigirTroca));
    }

    /**
     * Administrador de login curto ({@code praxis.admin.usuario}; no login basta
     * digitar o nome, o dominio e completado). Senha em {@code praxis.admin.senha}:
     * sem ela, nao e criado. Passa direto pelo agregado, sem o minimo de 6
     * caracteres que vale para cadastro pela tela - a senha vem da configuracao do
     * operador, e a responsabilidade por ela e dele.
     */
    private void carregarAdministrador() {
        if (adminSenha == null || adminSenha.isBlank()) {
            return;
        }
        String email = adminUsuario.contains("@") ? adminUsuario : adminUsuario + "@" + dominioEmail;
        if (usuarios.porEmail(email).isPresent()) {
            return;
        }
        usuarios.salvar(new Usuario(null, "Administrador", email, "ADMIN", Papel.CHEFE,
                codificador.codificar(adminSenha), false));
    }
}
