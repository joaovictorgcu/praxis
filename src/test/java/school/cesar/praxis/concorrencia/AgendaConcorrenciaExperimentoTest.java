package school.cesar.praxis.concorrencia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgendaConcorrenciaExperimentoTest {

    @Test
    @DisplayName("baseline sem sincronizacao permite violacao e versao sincronizada rejeita todas as tentativas extras")
    void experimentoSingleNode() {
        AgendaConcorrenciaExperimento experimento = new AgendaConcorrenciaExperimento();

        var baseline = experimento.executarBaseline(20, "Sala 01");
        var sincronizada = experimento.executarSincronizada(20, "Sala 01");

        assertTrue(baseline.houveViolacao(), "Baseline deve demonstrar violacao da regra de conflito");
        assertTrue(baseline.getAceitas() > 1, "Baseline deve aceitar mais de uma audiência conflitante");

        assertFalse(sincronizada.houveViolacao(), "Versao sincronizada deve impedir violacao");
        assertEquals(1, sincronizada.getAceitas(), "Apenas uma tentativa deve ser aceita");
        assertEquals(19, sincronizada.getRejeitadas(), "As demais tentativas devem ser rejeitadas");

        var cincoRodadas = experimento.executarCincoRodadas(20, "Sala 01");
        assertEquals(5, cincoRodadas.size(), "O experimento deve registrar 5 rodadas");
        assertTrue(cincoRodadas.stream().allMatch(r -> r.getAceitas() == 1), "Cada rodada sincronizada deve aceitar somente 1 audiência");
    }
}
