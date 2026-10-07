package school.cesar.praxis.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import school.cesar.praxis.domain.jurisdicao.Comarca;
import school.cesar.praxis.domain.jurisdicao.VaraJudicial;

class JurisdicaoTest {

    private final LocalDate hoje = LocalDate.of(2026, 10, 6);

    @Test
    void normalizaDadosEOpcionaisDaComarca() {
        var comarca = new Comarca(
            null,
            "  Comarca do Recife  ",
            " Recife ",
            " pe ",
            " TJPE ",
            " ",
            null,
            " foro@teste.jus.br ",
            null,
            " Observação ",
            hoje
        );
        assertEquals("Comarca do Recife", comarca.nome());
        assertEquals("Recife", comarca.municipio());
        assertEquals("PE", comarca.uf());
        assertEquals("foro@teste.jus.br", comarca.email());
        assertNull(comarca.endereco());
        assertEquals("Observação", comarca.observacoes());
    }

    @Test
    void rejeitaCamposObrigatoriosEstadoEmailELimites() {
        assertThrows(IllegalArgumentException.class, () ->
            new Comarca(null, " ", "Recife", "PE", "TJPE", null, null, null, null, null, hoje)
        );
        assertThrows(IllegalArgumentException.class, () ->
            new Comarca(null, "Recife", "", "PE", "TJPE", null, null, null, null, null, hoje)
        );
        assertThrows(IllegalArgumentException.class, () ->
            new Comarca(null, "Recife", "Recife", "XX", "TJPE", null, null, null, null, null, hoje)
        );
        assertThrows(IllegalArgumentException.class, () ->
            new Comarca(null, "Recife", "Recife", "PE", "", null, null, null, null, null, hoje)
        );
        assertThrows(IllegalArgumentException.class, () ->
            new Comarca(null, "Recife", "Recife", "PE", "TJPE", null, null, "invalido", null, null, hoje)
        );
        assertThrows(IllegalArgumentException.class, () ->
            new Comarca(null, "x".repeat(161), "Recife", "PE", "TJPE", null, null, null, null, null, hoje)
        );
        assertThrows(IllegalArgumentException.class, () ->
            new Comarca(
                null,
                "Recife",
                "Recife",
                "PE",
                "TJPE",
                null,
                null,
                null,
                null,
                "x".repeat(1001),
                hoje
            )
        );
    }

    @Test
    void varaExigeComarcaNomeECompetencia() {
        assertThrows(IllegalArgumentException.class, () ->
            new VaraJudicial(null, null, "Vara Cível", "Cível", null, null, null, null, null, hoje)
        );
        assertThrows(IllegalArgumentException.class, () ->
            new VaraJudicial(null, -1L, "Vara Cível", "Cível", null, null, null, null, null, hoje)
        );
        assertThrows(IllegalArgumentException.class, () ->
            new VaraJudicial(null, 1L, "", "Cível", null, null, null, null, null, hoje)
        );
        assertThrows(IllegalArgumentException.class, () ->
            new VaraJudicial(null, 1L, "Vara Cível", "", null, null, null, null, null, hoje)
        );
        var vara = new VaraJudicial(
            null,
            1L,
            " Vara Única ",
            " Competência geral ",
            null,
            null,
            null,
            null,
            null,
            hoje
        );
        assertEquals("Vara Única", vara.nome());
        assertEquals("Competência geral", vara.competencia());
    }
}
