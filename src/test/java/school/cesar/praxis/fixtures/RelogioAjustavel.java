package school.cesar.praxis.fixtures;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import school.cesar.praxis.domain.compartilhado.Relogio;

import java.time.LocalDate;

/**
 * Relogio do dominio que o teste pode fixar. Sem ajuste, e o relogio do
 * sistema - entao registrar este bean num contexto nao muda nenhum teste que
 * nao o use.
 *
 * <p>Existe porque parte das regras do prazo olha para "hoje": a dilacao so cabe
 * antes do vencimento (CPC art. 139, paragrafo unico) e suspensao e retomada
 * nao podem ser futuras. Cenario com data fixa precisa dizer que dia e hoje.
 */
public class RelogioAjustavel implements Relogio {

    private volatile LocalDate fixo;

    @Override
    public LocalDate hoje() {
        LocalDate dia = fixo;
        return dia == null ? LocalDate.now() : dia;
    }

    public void fixar(LocalDate dia) {
        this.fixo = dia;
    }

    public void voltarAoSistema() {
        this.fixo = null;
    }

    /** Importe no contexto de teste: vence o relogio do sistema por ser {@code @Primary}. */
    @TestConfiguration
    public static class Configuracao {

        @Bean
        @Primary
        public RelogioAjustavel relogioAjustavel() {
            return new RelogioAjustavel();
        }
    }
}
