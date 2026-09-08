package school.cesar.praxis.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.documento.Contestacao;
import school.cesar.praxis.domain.documento.GeradorDocumento;
import school.cesar.praxis.domain.documento.PeticaoInicial;
import school.cesar.praxis.domain.documento.Procuracao;
import school.cesar.praxis.domain.honorario.*;
import school.cesar.praxis.domain.notificacao.AdvogadoResponsavel;
import school.cesar.praxis.domain.notificacao.Notificador;
import school.cesar.praxis.domain.notificacao.ObservadorProcesso;
import school.cesar.praxis.domain.prazo.*;
import school.cesar.praxis.infrastructure.notificacao.NotificadorAuditoria;
import school.cesar.praxis.infrastructure.notificacao.NotificadorEmail;
import school.cesar.praxis.infrastructure.notificacao.NotificadorPainel;
import school.cesar.praxis.infrastructure.persistence.repository.NotificacaoJpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Composicao do dominio. As classes de dominio nao tem anotacao de framework:
 * quem as instancia e liga e esta configuracao, na borda da aplicacao.
 */
@Configuration
public class DominioConfig {

    /** Feriados nacionais de 2026 relevantes para o forense. */
    @Bean
    public CalendarioForense calendarioForense() {
        return new CalendarioForense(Set.of(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 16),
                LocalDate.of(2026, 2, 17),
                LocalDate.of(2026, 4, 3),
                LocalDate.of(2026, 4, 21),
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 4),
                LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 10, 12),
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 12, 25)));
    }

    @Bean
    public Relogio relogio() {
        return LocalDate::now;
    }

    @Bean
    public ContagemPrazoStrategy contagemDiasUteis(CalendarioForense calendario) {
        return new ContagemDiasUteis(calendario);
    }

    @Bean
    public ContagemPrazoStrategy contagemDiasCorridos(CalendarioForense calendario) {
        return new ContagemDiasCorridos(calendario);
    }

    @Bean
    public PoliticaDeAlerta politicaDeAlerta() {
        return PoliticaDeAlerta.padrao();
    }

    /** Cadeia do Decorator: painel -> e-mail -> auditoria. */
    @Bean
    public NotificadorPainel notificadorPainel() {
        return new NotificadorPainel();
    }

    @Bean
    public NotificadorEmail notificadorEmail(NotificadorPainel painel) {
        return new NotificadorEmail(painel);
    }

    @Bean
    public Notificador notificador(NotificadorEmail email, NotificacaoJpaRepository repositorio) {
        return new NotificadorAuditoria(email, repositorio);
    }

    @Bean
    public ObservadorProcesso observadorAdvogadoResponsavel(Notificador notificador) {
        return new AdvogadoResponsavel(notificador);
    }

    /** Motor de prazos com as duas estrategias e o observador ja assinado. */
    @Bean
    public MotorDePrazos motorDePrazos(List<ContagemPrazoStrategy> estrategias,
                                       PoliticaDeAlerta politica,
                                       ObservadorProcesso observador) {
        MotorDePrazos motor = new MotorDePrazos(estrategias, politica);
        motor.assinar(observador);
        return motor;
    }

    // --- Templates de peca (Template Method) ---

    @Bean
    public GeradorDocumento peticaoInicial() {
        return new PeticaoInicial();
    }

    @Bean
    public GeradorDocumento contestacao() {
        return new Contestacao();
    }

    @Bean
    public GeradorDocumento procuracao() {
        return new Procuracao();
    }

    // --- Estrategias de honorario ---

    @Bean
    public CalculoHonorarioStrategy honorarioFixo() {
        return new HonorarioFixo();
    }

    @Bean
    public CalculoHonorarioStrategy honorarioPorHora() {
        return new HonorarioPorHora();
    }

    @Bean
    public CalculoHonorarioStrategy honorarioQuotaLitis() {
        return new HonorarioQuotaLitis();
    }
}
