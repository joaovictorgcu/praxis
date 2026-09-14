package school.cesar.praxis.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import school.cesar.praxis.application.port.in.UsuariosUseCases;
import school.cesar.praxis.application.port.out.UsuarioRepositorio;
import school.cesar.praxis.domain.usuario.Papel;

/**
 * Usuarios de referencia para o sistema nascer acessivel: um chefe e os dois
 * advogados responsaveis pelos processos de exemplo (mesmas OABs de
 * {@link DadosDeExemplo}, para o segredo de justica ser demonstravel logado).
 *
 * <p>Roda em {@code ContextRefreshedEvent} porque sem usuario ninguem entra no
 * painel - inclusive nos testes HTTP. A senha inicial vem de propriedade e e
 * a mesma para todos; troque em producao.
 */
@Component
@ConditionalOnProperty(name = "praxis.usuarios-iniciais", havingValue = "true",
        matchIfMissing = true)
public class UsuariosIniciais implements SmartInitializingSingleton {

    private final UsuariosUseCases.CadastrarUsuario cadastrar;
    private final UsuarioRepositorio usuarios;
    private final String senhaInicial;
    private final boolean exigirTroca;

    public UsuariosIniciais(UsuariosUseCases.CadastrarUsuario cadastrar,
                            UsuarioRepositorio usuarios,
                            @Value("${praxis.senha-inicial:praxis123}") String senhaInicial,
                            @Value("${praxis.exigir-troca-senha-inicial:false}") boolean exigirTroca) {
        this.cadastrar = cadastrar;
        this.usuarios = usuarios;
        this.senhaInicial = senhaInicial;
        this.exigirTroca = exigirTroca;
    }

    /**
     * Roda depois de todos os singletons existirem e ANTES de o servidor web abrir a
     * porta (ContextRefreshedEvent dispara depois do servidor subir: um login no
     * primeiro segundo encontraria a tabela vazia). Roda tambem no @SpringBootTest.
     */
    @Override
    public void afterSingletonsInstantiated() {
        carregar();
    }

    public void carregar() {
        if (!usuarios.listar().isEmpty()) {
            return;
        }
        // Em dev a senha padrao e conhecida e nao exige troca (demonstracao); em prod
        // (praxis.exigir-troca-senha-inicial=true) o primeiro acesso cai na troca de senha.
        cadastrar.executar(new UsuariosUseCases.CadastrarUsuario.Comando(
                "Carla Mendes", "carla.mendes@praxis.adv.br", "PE00001", Papel.CHEFE, senhaInicial, exigirTroca));
        cadastrar.executar(new UsuariosUseCases.CadastrarUsuario.Comando(
                "Ana Beatriz Souza", "ana.souza@praxis.adv.br", "PE12345", Papel.ADVOGADO, senhaInicial, exigirTroca));
        cadastrar.executar(new UsuariosUseCases.CadastrarUsuario.Comando(
                "Bruno Carvalho", "bruno.carvalho@praxis.adv.br", "PE54321", Papel.ADVOGADO, senhaInicial, exigirTroca));
    }
}
