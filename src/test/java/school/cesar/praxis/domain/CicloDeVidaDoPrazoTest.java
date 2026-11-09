package school.cesar.praxis.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.cesar.praxis.domain.prazo.*;
import school.cesar.praxis.domain.processo.Advogado;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de unidade do ciclo de vida do prazo - dominio puro, sem Spring.
 * Calendario: 07/09/2026 (segunda) e feriado; fins de semana nao contam.
 */
class CicloDeVidaDoPrazoTest {

    private final CalendarioForense calendario = new CalendarioForense(Set.of(LocalDate.of(2026, 9, 7)));
    private final ContagemPrazoStrategy diasUteis = new ContagemDiasUteis(calendario);
    private final ContagemPrazoStrategy diasCorridos = new ContagemDiasCorridos(calendario);
    private final Advogado ana = new Advogado("Ana Souza", "ana@praxis.adv.br", "PE12345");
    private final NumeroCnj processo = NumeroCnj.de("0001234-56.2026.8.17.0001");

    private static LocalDate dia(int mes, int dia) {
        return LocalDate.of(2026, mes, dia);
    }

    /** Intimacao sexta 04/09; conta 08, 09, 10, 11 e 14/09: vence 14/09. */
    private Prazo contestacao() {
        return new Prazo(processo, "Contestação", dia(9, 4), 5, true, ana, diasUteis);
    }

    @Test
    @DisplayName("suspensao congela o saldo e a retomada restitui exatamente o que faltava (CPC art. 221)")
    void suspensaoERetomada() {
        Prazo prazo = contestacao();
        assertEquals(dia(9, 14), prazo.getVencimento());

        // 08 e 09 ja correram; de 10/09 em diante o prazo para. Faltavam 10, 11 e 14: saldo 3.
        prazo.suspender(dia(9, 10), "Convenção das partes (art. 313, II)", dia(9, 10));
        assertEquals(SituacaoPrazo.SUSPENSO, prazo.getSituacao());
        assertEquals(3, prazo.saldoNaSuspensao(diasUteis));
        assertEquals(3, prazo.diasRestantes(dia(9, 30), diasUteis), "suspenso, o saldo nao muda com o tempo");
        assertFalse(prazo.venceu(dia(9, 30)), "prazo suspenso nao vence");

        // Retoma em 16/09: suspensao de 10 a 15/09; os 3 dias voltam a correr em 16, 17 e 18.
        assertTrue(prazo.retomar(dia(9, 16), dia(9, 16), diasUteis));
        assertEquals(SituacaoPrazo.EM_ABERTO, prazo.getSituacao());
        assertEquals(dia(9, 18), prazo.getVencimento());
        assertEquals(dia(9, 14), prazo.getVencimentoOriginal(), "o vencimento original nunca muda");
        assertEquals(new PeriodoSuspensao(dia(9, 10), dia(9, 15), "Convenção das partes (art. 313, II)"),
                prazo.getSuspensoes().get(0));
        assertEquals(3, prazo.diasRestantes(dia(9, 15), diasUteis));
    }

    @Test
    @DisplayName("suspensao no proprio dia da intimacao devolve o prazo inteiro")
    void suspensaoNaIntimacao() {
        Prazo prazo = contestacao();
        prazo.suspender(dia(9, 4), "Falecimento do procurador (art. 313, I)", dia(9, 4));
        assertEquals(5, prazo.saldoNaSuspensao(diasUteis));

        prazo.retomar(dia(9, 10), dia(9, 10), diasUteis);
        // Conta 10, 11, 14, 15 e 16/09.
        assertEquals(dia(9, 16), prazo.getVencimento());
    }

    @Test
    @DisplayName("dias corridos: dia suspenso nao conta e vencimento em domingo prorroga para o dia util")
    void suspensaoEmDiasCorridos() {
        // 31/08 + 10 dias corridos = 10/09 (quinta).
        Prazo prazo = new Prazo(processo, "Recurso administrativo", dia(8, 31), 10, false, ana, diasCorridos);
        assertEquals(dia(9, 10), prazo.getVencimento());

        prazo.suspender(dia(9, 3), "Obstáculo criado pela parte contrária (art. 221)", dia(9, 3));
        prazo.retomar(dia(9, 6), dia(9, 6), diasCorridos);
        // 01 e 02 contam; 03 a 05 suspensos; 06 a 13 completam os 10 dias. 13/09 e domingo: vence 14/09.
        assertEquals(dia(9, 14), prazo.getVencimento());
    }

    @Test
    @DisplayName("dilacao soma dias e so cabe antes de encerrado o prazo regular (CPC art. 139, VI e par. unico)")
    void dilacao() {
        Prazo prazo = contestacao();
        assertTrue(prazo.dilatar(3, "Decisão de 11/09 dilatou o prazo", dia(9, 11), diasUteis));
        // 5 + 3 = 8 dias uteis: 08, 09, 10, 11, 14, 15, 16 e 17/09.
        assertEquals(8, prazo.diasEfetivos());
        assertEquals(dia(9, 17), prazo.getVencimento());

        IllegalStateException tardia = assertThrows(IllegalStateException.class,
                () -> prazo.dilatar(2, "Pedido fora do prazo", dia(9, 18), diasUteis));
        assertTrue(tardia.getMessage().contains("139"));
        assertThrows(IllegalArgumentException.class, () -> prazo.dilatar(0, "zero", dia(9, 11), diasUteis));
        assertThrows(IllegalArgumentException.class, () -> prazo.dilatar(2, " ", dia(9, 11), diasUteis));
    }

    @Test
    @DisplayName("prazo em dobro (CPC art. 183) dobra a contagem desde a abertura")
    void prazoEmDobro() {
        Prazo prazo = new Prazo(processo, "Contestação da Fazenda", dia(9, 4), 5, true, ana,
                PrerrogativaPrazo.FAZENDA_PUBLICA, diasUteis);
        assertEquals(10, prazo.diasEfetivos());
        // 08, 09, 10, 11, 14, 15, 16, 17, 18 e 21/09.
        assertEquals(dia(9, 21), prazo.getVencimento());
        assertEquals(dia(9, 21), prazo.getVencimentoOriginal());
    }

    @Test
    @DisplayName("suspensao exige motivo e nao pode ser futura, anterior a intimacao ou depois do vencimento")
    void regrasDaSuspensao() {
        Prazo prazo = contestacao();
        assertThrows(IllegalArgumentException.class, () -> prazo.suspender(dia(9, 10), " ", dia(9, 10)));
        assertThrows(IllegalArgumentException.class, () -> prazo.suspender(dia(9, 11), "futura", dia(9, 10)));
        assertThrows(IllegalArgumentException.class, () -> prazo.suspender(dia(9, 3), "antes", dia(9, 10)));
        assertThrows(IllegalStateException.class, () -> prazo.suspender(dia(9, 15), "vencido", dia(9, 20)));
        assertEquals(SituacaoPrazo.EM_ABERTO, prazo.getSituacao(), "tentativa recusada nao muda o prazo");
    }

    @Test
    @DisplayName("retomada deve ser posterior ao inicio da suspensao e nao pode ser futura")
    void regrasDaRetomada() {
        Prazo prazo = contestacao();
        assertThrows(IllegalStateException.class, () -> prazo.retomar(dia(9, 12), dia(9, 12), diasUteis),
                "so retoma prazo suspenso");
        prazo.suspender(dia(9, 10), "Convenção das partes", dia(9, 10));
        assertThrows(IllegalArgumentException.class, () -> prazo.retomar(dia(9, 10), dia(9, 12), diasUteis));
        assertThrows(IllegalArgumentException.class, () -> prazo.retomar(dia(9, 14), dia(9, 12), diasUteis));
        assertEquals(SituacaoPrazo.SUSPENSO, prazo.getSituacao());
    }

    @Test
    @DisplayName("suspensoes nao se sobrepoem; a segunda soma dias parados a primeira")
    void duasSuspensoes() {
        Prazo prazo = contestacao();
        prazo.suspender(dia(9, 9), "Primeira", dia(9, 9));
        prazo.retomar(dia(9, 11), dia(9, 11), diasUteis);      // parado 09 e 10
        assertThrows(IllegalArgumentException.class, () -> prazo.suspender(dia(9, 10), "Sobreposta", dia(9, 14)));
        prazo.suspender(dia(9, 14), "Segunda", dia(9, 14));
        prazo.retomar(dia(9, 16), dia(9, 16), diasUteis);      // parado 14 e 15
        // Contam 08, 11, 16, 17 e 18/09.
        assertEquals(dia(9, 18), prazo.getVencimento());
        assertEquals(2, prazo.getSuspensoes().size());
    }

    @Test
    @DisplayName("prazo suspenso nao entra na varredura nem pode ser cumprido (CPC art. 314)")
    void suspensoForaDaVarredura() {
        MotorDePrazos motor = new MotorDePrazos(List.of(diasUteis, diasCorridos), PoliticaDeAlerta.padrao());
        Prazo prazo = contestacao();
        prazo.suspender(dia(9, 10), "Convenção das partes", dia(9, 10));

        assertTrue(motor.avaliar(prazo, dia(9, 11)).isEmpty());
        IllegalStateException cumprir = assertThrows(IllegalStateException.class, () -> prazo.cumprir(dia(9, 11)));
        assertTrue(cumprir.getMessage().contains("314"));
    }

    @Test
    @DisplayName("mudanca de vencimento reinicia a regua: o mesmo nivel volta a ser emitido para a nova data")
    void reguaReiniciada() {
        MotorDePrazos motor = new MotorDePrazos(List.of(diasUteis, diasCorridos), PoliticaDeAlerta.padrao());
        Prazo prazo = contestacao();

        assertEquals(NivelAlerta.URGENTE, motor.avaliar(prazo, dia(9, 9)).orElseThrow().nivel());
        assertTrue(prazo.jaAlertou(NivelAlerta.URGENTE));

        prazo.dilatar(5, "Decisão dilatou o prazo", dia(9, 9), diasUteis);   // vence 21/09
        assertTrue(prazo.getAlertasEmitidos().isEmpty());
        // Em 16/09 faltam 17, 18 e 21: URGENTE de novo, agora para 21/09.
        AlertaPrazo novo = motor.avaliar(prazo, dia(9, 16)).orElseThrow();
        assertEquals(NivelAlerta.URGENTE, novo.nivel());
        assertEquals(dia(9, 21), novo.vencimento());
    }

    @Test
    @DisplayName("cancelar e reabrir: a reabertura volta a suspenso quando havia suspensao aberta")
    void cancelarEReabrir() {
        Prazo prazo = contestacao();
        prazo.suspender(dia(9, 10), "Convenção das partes", dia(9, 10));
        prazo.cancelar("Lançado em duplicidade");
        assertEquals(SituacaoPrazo.CANCELADO, prazo.getSituacao());
        assertThrows(IllegalStateException.class, () -> prazo.cancelar("de novo"));

        prazo.reabrir("Cancelado por engano");
        assertEquals(SituacaoPrazo.SUSPENSO, prazo.getSituacao());

        Prazo cumprido = contestacao();
        cumprido.cumprir(dia(9, 11));
        assertThrows(IllegalStateException.class, () -> cumprido.cancelar("depois de cumprido"));
        cumprido.reabrir("Cumprimento registrado no prazo errado");
        assertEquals(SituacaoPrazo.EM_ABERTO, cumprido.getSituacao());
        assertNull(cumprido.getCumpridoEm());
        assertThrows(IllegalArgumentException.class, () -> cumprido.cancelar(""));
    }

    @Test
    @DisplayName("contagem contra suspensao sem fim para no teto de seguranca em vez de travar")
    void tetoDeSeguranca() {
        assertThrows(IllegalStateException.class,
                () -> diasUteis.calcularVencimento(dia(9, 4), 5, dia -> true));
        assertThrows(IllegalStateException.class,
                () -> diasCorridos.calcularVencimento(dia(9, 4), 5, dia -> true));
    }
}
