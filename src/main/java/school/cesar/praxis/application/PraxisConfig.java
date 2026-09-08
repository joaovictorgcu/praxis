package school.cesar.praxis.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import school.cesar.praxis.domain.notificacao.Notificador;
import school.cesar.praxis.domain.notificacao.NotificadorAuditoria;
import school.cesar.praxis.domain.notificacao.NotificadorEmail;
import school.cesar.praxis.domain.prazo.CalendarioForense;
import school.cesar.praxis.domain.prazo.ContagemDiasUteis;
import school.cesar.praxis.domain.prazo.ContagemPrazoStrategy;
import school.cesar.praxis.infrastructure.notificacao.PainelCompartilhado;

import java.time.LocalDate;
import java.util.Set;

/** Composicao das estrategias e da cadeia de decorators de notificacao. */
@Configuration
public class PraxisConfig {

    @Bean
    public CalendarioForense calendarioForense() {
        return new CalendarioForense(Set.of(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 4, 3),
                LocalDate.of(2026, 4, 21),
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 10, 12),
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 12, 25)));
    }

    @Bean
    public ContagemPrazoStrategy contagemPadrao(CalendarioForense calendario) {
        return new ContagemDiasUteis(calendario);
    }

    /** Painel decorado com e-mail e trilha de auditoria. */
    @Bean
    public Notificador notificador(PainelCompartilhado painel) {
        return new NotificadorAuditoria(new NotificadorEmail(painel));
    }
}
