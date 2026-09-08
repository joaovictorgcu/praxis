package school.cesar.praxis.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Liga o Cucumber ao contexto Spring: os cenarios exercitam os casos de uso reais,
 * com banco relacional em memoria e sem a carga de dados de demonstracao.
 */
@CucumberContextConfiguration
@SpringBootTest
@TestPropertySource(properties = {
        "praxis.dados-exemplo=false",
        "spring.datasource.url=jdbc:h2:mem:praxis-bdd;DB_CLOSE_DELAY=-1"
})
public class ConfiguracaoCucumberSpring {
}
