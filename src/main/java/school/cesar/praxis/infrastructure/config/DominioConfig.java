package school.cesar.praxis.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.documento.Contestacao;
import school.cesar.praxis.domain.documento.GeradorDocumento;
import school.cesar.praxis.domain.documento.PeticaoInicial;
import school.cesar.praxis.domain.documento.Procuracao;
import school.cesar.praxis.domain.feriado.FonteDeFeriados;
import school.cesar.praxis.domain.feriado.Jurisdicao;
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

/**
 * Composicao do dominio. As classes de dominio nao tem anotacao de framework:
 * quem as instancia e liga e esta configuracao, na borda da aplicacao.
 */
@Configuration
public class DominioConfig {

    /**
     * Foro em que o escritorio atua. Define quais feriados estaduais e
     * comarcais entram na contagem de prazo.
     */
    @Bean
    public Jurisdicao foroDoEscritorio(@Value("${praxis.foro.uf:PE}") String uf,
                                       @Value("${praxis.foro.comarca:Recife}") String comarca) {
        return Jurisdicao.de(uf, comarca);
    }

    /**
     * Calendario montado sobre o cadastro de feriados, e nao sobre uma lista
     * fixa: cadastrar feriado passa a valer sem reiniciar a aplicacao.
     */
    @Bean
    public CalendarioForense calendarioForense(FonteDeFeriados feriados, Jurisdicao foro) {
        return CalendarioForense.doForo(feriados, foro);
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

    @Bean
    public MotorDeHonorarios motorDeHonorarios(List<CalculoHonorarioStrategy> estrategias) {
        return new MotorDeHonorarios(estrategias);
    }
}
