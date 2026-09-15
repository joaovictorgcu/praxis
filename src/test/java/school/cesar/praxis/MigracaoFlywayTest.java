package school.cesar.praxis;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.application.port.in.UsuariosUseCases;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.domain.usuario.Papel;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sobe a aplicacao como em producao no que diz respeito ao esquema: Flyway
 * cria as tabelas a partir de {@code db/migration} e o Hibernate apenas
 * valida ({@code ddl-auto=validate}). O banco e H2 em modo PostgreSQL, entao
 * o que se prova aqui e que a migracao e as entidades concordam em tabelas,
 * colunas e tipos - nao a sintaxe especifica do PostgreSQL real.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:praxis-flyway;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "praxis.dados-exemplo=false"
})
class MigracaoFlywayTest {

    @Autowired
    private Flyway flyway;
    @Autowired
    private ProcessosUseCases.CadastrarProcesso cadastrarProcesso;
    @Autowired
    private DocumentosUseCases.GerarDocumento gerarDocumento;
    @Autowired
    private DocumentosUseCases.ListarDocumentos listarDocumentos;
    @Autowired
    private UsuariosUseCases.Autenticar autenticar;
    @Autowired
    private UsuariosUseCases.CadastrarUsuario cadastrarUsuario;

    @Test
    @DisplayName("migracao V1 aplicada e validada pelo Hibernate; fluxo basico grava e le")
    void esquemaVersionado() {
        assertEquals(1, flyway.info().applied().length);
        assertEquals("1", flyway.info().current().getVersion().getVersion());

        // Usuarios iniciais entraram pela carga (tabela usuario com senha_provisoria).
        assertNotNull(autenticar.executar(new UsuariosUseCases.Autenticar.Comando(
                "carla.mendes@praxis.adv.br", "praxis123")));
        cadastrarUsuario.executar(new UsuariosUseCases.CadastrarUsuario.Comando(
                "Novo", "novo@praxis.adv.br", "PE11111", Papel.ADVOGADO, "senha123"));

        String numero = "0001111-11.2026.8.17.0011";
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                numero, "Cliente Flyway", "Recife", true, "Ana Souza", "ana@praxis.adv.br", "PE12345"));
        gerarDocumento.executar(new DocumentosUseCases.GerarDocumento.Comando(
                numero, TipoDocumento.PETICAO_INICIAL, Map.of("fatos", "x"), "PE12345"));
        assertEquals(1, listarDocumentos.executar(numero).size());
    }
}
