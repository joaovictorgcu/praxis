package school.cesar.praxis.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import school.cesar.praxis.domain.kanban.*;

class KanbanTest {

    @Test
    void validaNomesCoresPosicoesELimites() {
        var coluna = new ColunaKanban(1L, " Revisão ", "#Ab1234", 0, false, 0);
        assertEquals("Revisão", coluna.nome());
        assertEquals("#ab1234", coluna.cor());
        assertThrows(IllegalArgumentException.class, () ->
            new ColunaKanban(null, " ", "#123456", 0, false, 0)
        );
        assertThrows(IllegalArgumentException.class, () ->
            new ColunaKanban(null, "Coluna", "red;display:none", 0, false, 0)
        );
        assertThrows(IllegalArgumentException.class, () ->
            new ColunaKanban(null, "Coluna", "#123456", -1, false, 0)
        );
        assertThrows(IllegalArgumentException.class, () -> new ItemChecklist("", false));
        assertThrows(IllegalArgumentException.class, () -> new ItemChecklist("x".repeat(201), false));
    }

    @Test
    void movimentacaoMantemDadosEAtualizaData() {
        var hoje = LocalDate.of(2026, 10, 6);
        var t = new Tarefa(
            1L,
            "0001234-56.2026.8.17.0001",
            1L,
            " Revisar ",
            " Orientações ",
            PrioridadeTarefa.ALTA,
            2L,
            hoje,
            List.of(new ItemChecklist("Conferir", true)),
            0,
            hoje,
            hoje,
            3
        );
        var movida = t.mover(2L, 1, hoje.plusDays(1));
        assertEquals(t.titulo(), movida.titulo());
        assertEquals(t.checklist(), movida.checklist());
        assertEquals(t.criadaEm(), movida.criadaEm());
        assertEquals(hoje.plusDays(1), movida.atualizadaEm());
        assertEquals(2L, movida.colunaId());
        assertEquals(1, movida.ordem());
    }
}
