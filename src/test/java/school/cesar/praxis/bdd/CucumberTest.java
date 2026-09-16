package school.cesar.praxis.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Runner dos cenarios BDD: automatiza os arquivos .feature com o Cucumber.
 *
 * <p>O relatorio HTML e o JSON saem em {@code target/cucumber-reports/} e sao
 * o que a CI publica como artefato - e por onde se le quais cenarios rodaram
 * sem abrir o log do build.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "school.cesar.praxis.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, summary,"
                + " html:target/cucumber-reports/cucumber.html,"
                + " json:target/cucumber-reports/cucumber.json")
public class CucumberTest {
}
