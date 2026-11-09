package school.cesar.praxis.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import school.cesar.praxis.fixtures.RelogioAjustavel;

/**
 * Liga o Cucumber ao contexto Spring: os cenarios exercitam os casos de uso reais,
 * com banco relacional em memoria e sem a carga de dados de demonstracao.
 *
 * <p>O {@link RelogioAjustavel} e o relogio do dominio nos cenarios: sem ajuste,
 * e o do sistema; o cenario que precisa de um "hoje" fixo diz qual e.
 */
@CucumberContextConfiguration
@SpringBootTest
@Import(RelogioAjustavel.Configuracao.class)
@TestPropertySource(properties = {
        "praxis.dados-exemplo=false",
        "spring.datasource.url=jdbc:h2:mem:praxis-bdd;DB_CLOSE_DELAY=-1"
})
public class ConfiguracaoCucumberSpring {
}
