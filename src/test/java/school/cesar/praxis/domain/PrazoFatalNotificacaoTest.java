package school.cesar.praxis.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.cesar.praxis.domain.notificacao.*;
import school.cesar.praxis.domain.prazo.*;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.domain.processo.Processo;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BDD: Dado um processo com prazo fatal em 5 dias uteis, quando faltarem 3 dias,
 * entao o advogado responsavel deve ser notificado.
 */
class PrazoFatalNotificacaoTest {

    private final CalendarioForense calendario = new CalendarioForense(Set.of());
    private final ContagemPrazoStrategy diasUteis = new ContagemDiasUteis(calendario);

    @Test
    @DisplayName("prazo fatal a 3 dias uteis do vencimento notifica o responsavel")
    void notificaResponsavelQuandoFaltamTresDiasUteis() {
        // Dado: intimacao na terca 08/09/2026, prazo fatal de 5 dias uteis
        LocalDate intimacao = LocalDate.of(2026, 9, 8);
        NotificadorPainel painel = new NotificadorPainel();
        NotificadorEmail email = new NotificadorEmail(painel);
        NotificadorAuditoria notificador = new NotificadorAuditoria(email);

        Processo processo = new Processo(new NumeroCnj("0001234-56.2026.8.17.0001"), "Cliente Alfa", false);
        processo.assinar(new AdvogadoResponsavel("Dra. Ana", "ana@praxis.adv.br", notificador));

        Prazo prazo = new Prazo("Contestacao", intimacao, 5, true, diasUteis);
        processo.adicionarPrazo(prazo);

        // Termo inicial 09/09; 5 dias uteis: 09, 10, 11, 14, 15 -> vence 15/09/2026
        assertEquals(LocalDate.of(2026, 9, 15), prazo.getVencimento());

        // Quando ainda faltam 5 dias uteis: nenhum alerta
        assertEquals(0, processo.verificarPrazos(intimacao, diasUteis));
        assertTrue(painel.getEntregues().isEmpty());

        // Quando faltam 3 dias uteis (de 10/09 restam 11, 14, 15)
        int alertas = processo.verificarPrazos(LocalDate.of(2026, 9, 10), diasUteis);

        // Entao o responsavel foi notificado, com e-mail e trilha de auditoria
        assertEquals(1, alertas);
        assertEquals(1, painel.getEntregues().size());
        Notificacao entregue = painel.getEntregues().get(0);
        assertEquals("ana@praxis.adv.br", entregue.destinatario());
        assertTrue(entregue.assunto().contains("PRAZO FATAL em 3 dia(s)"));
        assertEquals(1, email.getEmailsEnviados().size());
        assertEquals(1, notificador.getTrilha().size());
    }

    @Test
    @DisplayName("prazo cumprido deixa de gerar alerta")
    void prazoCumpridoNaoAlerta() {
        Processo processo = new Processo(new NumeroCnj("0001234-56.2026.8.17.0002"), "Cliente Beta", false);
        NotificadorPainel painel = new NotificadorPainel();
        processo.assinar(new AdvogadoResponsavel("Dr. Bruno", "bruno@praxis.adv.br", painel));

        Prazo prazo = new Prazo("Recurso", LocalDate.of(2026, 9, 8), 5, true, diasUteis);
        processo.adicionarPrazo(prazo);
        prazo.marcarCumprido();

        assertEquals(0, processo.verificarPrazos(LocalDate.of(2026, 9, 14), diasUteis));
        assertTrue(painel.getEntregues().isEmpty());
    }

    @Test
    @DisplayName("dias corridos e dias uteis produzem vencimentos diferentes")
    void regimesDeContagemDivergem() {
        LocalDate intimacao = LocalDate.of(2026, 9, 8);
        Prazo uteis = new Prazo("Manifestacao", intimacao, 15, false, diasUteis);
        Prazo corridos = new Prazo("Manifestacao", intimacao, 15, false, new ContagemDiasCorridos(calendario));

        assertTrue(uteis.getVencimento().isAfter(corridos.getVencimento()));
        assertEquals(LocalDate.of(2026, 9, 23), corridos.getVencimento());
        assertEquals(LocalDate.of(2026, 9, 29), uteis.getVencimento());
    }
}
