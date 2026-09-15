package school.cesar.praxis.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.cesar.praxis.domain.prazo.*;
import school.cesar.praxis.domain.processo.Advogado;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regressao do aviso de perda: prazo que ja recebeu "vence hoje" no dia do
 * vencimento precisa, no dia seguinte, gerar o aviso de VENCIDO - antes os dois
 * usavam o mesmo marco e o segundo nunca saia.
 */
class AvisoDePerdaTest {

    private final ContagemPrazoStrategy corridos = new ContagemDiasCorridos(
            new CalendarioForense(Set.of()));
    private final MotorDePrazos motor = new MotorDePrazos(List.of(corridos), PoliticaDeAlerta.padrao());

    private Prazo prazoFatal(LocalDate intimacao, int dias) {
        return new Prazo(NumeroCnj.de("0001234-56.2026.8.17.0001"), "Contestacao", intimacao, dias, true,
                new Advogado("Ana", "ana@praxis.adv.br", "PE12345"), corridos);
    }

    @Test
    @DisplayName("vence hoje no dia D e aviso de perda em D+1 sao marcos distintos")
    void venceHojeDepoisVencido() {
        Prazo prazo = prazoFatal(LocalDate.of(2026, 3, 2), 3);
        LocalDate vencimento = prazo.getVencimento();

        Optional<AlertaPrazo> hoje = motor.avaliar(prazo, vencimento);
        assertTrue(hoje.isPresent());
        assertEquals(NivelAlerta.VENCE_HOJE, hoje.get().nivel());
        assertFalse(hoje.get().vencido());

        Optional<AlertaPrazo> perdido = motor.avaliar(prazo, vencimento.plusDays(1));
        assertTrue(perdido.isPresent(), "aviso de perda nao saiu apos o 'vence hoje'");
        assertEquals(NivelAlerta.VENCIDO, perdido.get().nivel());
        assertTrue(perdido.get().vencido());

        // E so uma vez: a varredura seguinte nao repete.
        assertTrue(motor.avaliar(prazo, vencimento.plusDays(2)).isEmpty());
    }

    @Test
    @DisplayName("prazo nao fatal vencido segue a politica: sem alerta na politica padrao")
    void naoFatalRespeitaPolitica() {
        Prazo comum = new Prazo(NumeroCnj.de("0001234-56.2026.8.17.0001"), "Manifestacao",
                LocalDate.of(2026, 3, 2), 3, false,
                new Advogado("Ana", "ana@praxis.adv.br", "PE12345"), corridos);
        assertTrue(motor.avaliar(comum, comum.getVencimento().plusDays(5)).isEmpty());

        MotorDePrazos inclusivo = new MotorDePrazos(List.of(corridos), PoliticaDeAlerta.inclusiva());
        Optional<AlertaPrazo> alerta = inclusivo.avaliar(comum, comum.getVencimento().plusDays(5));
        assertTrue(alerta.isPresent());
        assertEquals(NivelAlerta.VENCIDO, alerta.get().nivel());
    }
}
