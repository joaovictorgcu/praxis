package school.cesar.praxis.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.cesar.praxis.domain.notificacao.EventoProcesso;
import school.cesar.praxis.domain.notificacao.ObservadorProcesso;
import school.cesar.praxis.domain.prazo.*;
import school.cesar.praxis.domain.processo.Advogado;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Testes de unidade do motor de prazos - dominio puro, sem Spring. */
class MotorDePrazosTest {

    private final CalendarioForense calendario = new CalendarioForense(Set.of(
            LocalDate.of(2026, 9, 7)));
    private final ContagemPrazoStrategy diasUteis = new ContagemDiasUteis(calendario);
    private final ContagemPrazoStrategy diasCorridos = new ContagemDiasCorridos(calendario);
    private final Advogado ana = new Advogado("Ana Souza", "ana@praxis.adv.br", "PE12345");
    private final NumeroCnj processo = NumeroCnj.de("0001234-56.2026.8.17.0001");

    private MotorDePrazos motorComObservador(List<EventoProcesso> capturados) {
        MotorDePrazos motor = new MotorDePrazos(List.of(diasUteis, diasCorridos), PoliticaDeAlerta.padrao());
        motor.assinar(capturados::add);
        return motor;
    }

    @Test
    @DisplayName("contagem em dias uteis pula fim de semana e feriado")
    void contagemEmDiasUteis() {
        // Intimacao sexta 04/09/2026; 07/09 e feriado; termo inicial 08/09 (terca)
        Prazo prazo = new Prazo(processo, "Contestacao", LocalDate.of(2026, 9, 4), 5, true, ana, diasUteis);

        // 08, 09, 10, 11 e 14 (12 e 13 sao fim de semana)
        assertEquals(LocalDate.of(2026, 9, 14), prazo.getVencimento());
        assertEquals(RegimeContagem.DIAS_UTEIS, prazo.getRegime());
    }

    @Test
    @DisplayName("dias corridos vencendo em feriado prorroga para o proximo dia util")
    void diasCorridosProrrogaVencimento() {
        // 31/08 + 7 dias = 07/09 (feriado) -> prorroga para 08/09
        Prazo prazo = new Prazo(processo, "Recurso administrativo",
                LocalDate.of(2026, 8, 31), 7, false, ana, diasCorridos);

        assertEquals(LocalDate.of(2026, 9, 8), prazo.getVencimento());
    }

    @Test
    @DisplayName("alerta escalona por dias restantes e nao repete o mesmo nivel")
    void alertaEscalonaENaoRepete() {
        List<EventoProcesso> eventos = new ArrayList<>();
        MotorDePrazos motor = motorComObservador(eventos);

        Prazo prazo = new Prazo(processo, "Contestacao", LocalDate.of(2026, 9, 4), 5, true, ana, diasUteis);
        assertEquals(LocalDate.of(2026, 9, 14), prazo.getVencimento());

        // Faltam 5 dias uteis (09, 10, 11, 14 a partir de 08/09 sao 4) -> em 04/09 restam 5
        Optional<AlertaPrazo> atencao = motor.avaliar(prazo, LocalDate.of(2026, 9, 4));
        assertTrue(atencao.isPresent());
        assertEquals(NivelAlerta.ATENCAO, atencao.get().nivel());

        // Mesmo dia, segunda varredura: nada de novo (idempotencia por marco)
        assertTrue(motor.avaliar(prazo, LocalDate.of(2026, 9, 4)).isEmpty());

        // Faltando 3 dias uteis -> URGENTE
        Optional<AlertaPrazo> urgente = motor.avaliar(prazo, LocalDate.of(2026, 9, 9));
        assertTrue(urgente.isPresent());
        assertEquals(NivelAlerta.URGENTE, urgente.get().nivel());
        assertEquals(3, urgente.get().diasRestantes());

        // Faltando 1 dia util -> CRITICO
        Optional<AlertaPrazo> critico = motor.avaliar(prazo, LocalDate.of(2026, 9, 11));
        assertTrue(critico.isPresent());
        assertEquals(NivelAlerta.CRITICO, critico.get().nivel());

        assertEquals(3, eventos.size());
        assertTrue(eventos.get(1) instanceof EventoProcesso.PrazoEmRisco);
    }

    @Test
    @DisplayName("prazo comum nao alerta na politica padrao")
    void prazoComumNaoAlerta() {
        List<EventoProcesso> eventos = new ArrayList<>();
        MotorDePrazos motor = motorComObservador(eventos);

        Prazo comum = new Prazo(processo, "Manifestacao", LocalDate.of(2026, 9, 4), 5, false, ana, diasUteis);

        assertTrue(motor.avaliar(comum, LocalDate.of(2026, 9, 11)).isEmpty());
        assertTrue(eventos.isEmpty());
    }

    @Test
    @DisplayName("prazo cumprido sai da varredura")
    void prazoCumpridoNaoAlerta() {
        MotorDePrazos motor = new MotorDePrazos(List.of(diasUteis), PoliticaDeAlerta.padrao());
        Prazo prazo = new Prazo(processo, "Contestacao", LocalDate.of(2026, 9, 4), 5, true, ana, diasUteis);
        prazo.cumprir(LocalDate.of(2026, 9, 10));

        assertTrue(motor.avaliar(prazo, LocalDate.of(2026, 9, 11)).isEmpty());
        assertThrows(IllegalStateException.class, () -> prazo.cumprir(LocalDate.of(2026, 9, 11)));
    }

    @Test
    @DisplayName("prazo vencido em aberto gera alerta de perda")
    void prazoVencidoGeraAlerta() {
        List<EventoProcesso> eventos = new ArrayList<>();
        MotorDePrazos motor = motorComObservador(eventos);

        Prazo prazo = new Prazo(processo, "Contestacao", LocalDate.of(2026, 9, 4), 5, true, ana, diasUteis);
        Optional<AlertaPrazo> alerta = motor.avaliar(prazo, LocalDate.of(2026, 9, 20));

        assertTrue(alerta.isPresent());
        assertTrue(alerta.get().vencido());
        assertEquals(0, alerta.get().diasRestantes());
        assertTrue(eventos.get(0) instanceof EventoProcesso.PrazoVencido);
    }

    @Test
    @DisplayName("motor recusa regime sem estrategia registrada")
    void regimeSemEstrategia() {
        MotorDePrazos motor = new MotorDePrazos(List.of(diasUteis), PoliticaDeAlerta.padrao());
        assertThrows(IllegalStateException.class,
                () -> motor.estrategiaPara(RegimeContagem.DIAS_CORRIDOS));
    }

    @Test
    @DisplayName("politica inclusiva alerta prazo comum")
    void politicaInclusiva() {
        MotorDePrazos motor = new MotorDePrazos(List.of(diasUteis), PoliticaDeAlerta.inclusiva());
        Prazo comum = new Prazo(processo, "Manifestacao", LocalDate.of(2026, 9, 4), 5, false, ana, diasUteis);

        assertTrue(motor.avaliar(comum, LocalDate.of(2026, 9, 11)).isPresent());
    }

    @Test
    @DisplayName("prazo exige dias positivos e responsavel")
    void invariantesDoPrazo() {
        assertThrows(IllegalArgumentException.class,
                () -> new Prazo(processo, "X", LocalDate.of(2026, 9, 4), 0, true, ana, diasUteis));
        assertThrows(IllegalArgumentException.class,
                () -> new Prazo(processo, "X", LocalDate.of(2026, 9, 4), 5, true, null, diasUteis));
        assertThrows(IllegalArgumentException.class, () -> NumeroCnj.de("123"));
    }
}
