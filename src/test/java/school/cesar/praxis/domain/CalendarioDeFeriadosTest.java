package school.cesar.praxis.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.cesar.praxis.domain.feriado.*;
import school.cesar.praxis.domain.prazo.CalendarioForense;
import school.cesar.praxis.domain.prazo.ContagemDiasUteis;
import school.cesar.praxis.domain.prazo.ContagemPrazoStrategy;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Testes de unidade do cadastro de feriados - dominio puro, sem Spring. */
class CalendarioDeFeriadosTest {

    private final Jurisdicao recife = Jurisdicao.de("PE", "Recife");

    /** FonteDeFeriados e interface de um metodo: o dublê e a propria lista. */
    private CalendarioForense calendarioCom(Feriado... feriados) {
        return CalendarioForense.doForo(() -> List.of(feriados), recife);
    }

    // --- Strategy de recorrencia ---

    @Test
    @DisplayName("feriado de data unica vale so naquele ano")
    void dataUnicaNaoRepete() {
        RegraRecorrencia regra = new DataUnica(LocalDate.of(2026, 10, 15));

        assertTrue(regra.incideEm(LocalDate.of(2026, 10, 15)));
        assertFalse(regra.incideEm(LocalDate.of(2027, 10, 15)));
    }

    @Test
    @DisplayName("feriado anual repete no mesmo dia e mes de qualquer ano")
    void recorrenciaAnualRepete() {
        RegraRecorrencia regra = RecorrenciaAnualFixa.de(LocalDate.of(2026, 12, 25));

        assertTrue(regra.incideEm(LocalDate.of(2026, 12, 25)));
        assertTrue(regra.incideEm(LocalDate.of(2031, 12, 25)));
        assertFalse(regra.incideEm(LocalDate.of(2026, 12, 24)));
    }

    @Test
    @DisplayName("recorrencia anual sobrevive a ida e volta da data de referencia")
    void referenciaAnualPreservaDiaEMes() {
        RegraRecorrencia original = new RecorrenciaAnualFixa(MonthDay.of(2, 29));
        RegraRecorrencia reconstruida = RecorrenciaAnualFixa.de(original.dataDeReferencia());

        assertEquals(original, reconstruida);
    }

    // --- Abrangencia ---

    @Test
    @DisplayName("abrangencia filtra por UF e por comarca")
    void abrangenciaFiltraPeloForo() {
        assertTrue(Abrangencia.nacional().alcanca(recife));
        assertTrue(Abrangencia.estadual("pe").alcanca(recife));
        assertFalse(Abrangencia.estadual("SP").alcanca(recife));
        assertTrue(Abrangencia.comarcal("Recife").alcanca(recife));
        assertFalse(Abrangencia.comarcal("Olinda").alcanca(recife));
    }

    @Test
    @DisplayName("feriado nacional nao guarda valor de referencia")
    void nacionalDescartaValor() {
        assertNull(new Abrangencia(Abrangencia.Nivel.NACIONAL, "PE").valor());
    }

    @Test
    @DisplayName("abrangencia restrita exige o valor de referencia")
    void abrangenciaRestritaExigeValor() {
        assertThrows(IllegalArgumentException.class,
                () -> new Abrangencia(Abrangencia.Nivel.ESTADUAL, " "));
        assertThrows(IllegalArgumentException.class,
                () -> new Abrangencia(Abrangencia.Nivel.COMARCAL, null));
    }

    // --- Composite do calendario ---

    @Test
    @DisplayName("calendario do foro respeita a abrangencia do feriado")
    void calendarioRespeitaAbrangencia() {
        LocalDate quinta = LocalDate.of(2026, 10, 15);

        CalendarioForense comFeriadoDeOlinda = calendarioCom(new Feriado(
                "Padroeira de Olinda", new DataUnica(quinta), Abrangencia.comarcal("Olinda")));
        CalendarioForense comFeriadoDoRecife = calendarioCom(new Feriado(
                "Aniversario do Recife", new DataUnica(quinta), Abrangencia.comarcal("Recife")));

        assertTrue(comFeriadoDeOlinda.isDiaUtil(quinta), "feriado de outra comarca nao suspende");
        assertFalse(comFeriadoDoRecife.isDiaUtil(quinta), "feriado da comarca do foro suspende");
    }

    @Test
    @DisplayName("fim de semana e recesso forense valem sem feriado cadastrado")
    void regrasDeLeiIndependemDoCadastro() {
        CalendarioForense vazio = calendarioCom();

        assertFalse(vazio.isDiaUtil(LocalDate.of(2026, 10, 17)), "sabado");
        assertFalse(vazio.isDiaUtil(LocalDate.of(2026, 10, 18)), "domingo");
        assertFalse(vazio.isDiaUtil(LocalDate.of(2026, 12, 26)), "recesso do art. 220");
        assertFalse(vazio.isDiaUtil(LocalDate.of(2027, 1, 15)), "recesso do art. 220");
        assertTrue(vazio.isDiaUtil(LocalDate.of(2026, 10, 15)), "quinta comum");
    }

    @Test
    @DisplayName("proximo dia util pula o feriado cadastrado")
    void proximoDiaUtilPulaFeriadoCadastrado() {
        CalendarioForense calendario = calendarioCom(new Feriado(
                "Ponto facultativo", new DataUnica(LocalDate.of(2026, 10, 15)),
                Abrangencia.nacional()));

        assertEquals(LocalDate.of(2026, 10, 16),
                calendario.proximoDiaUtil(LocalDate.of(2026, 10, 15)));
    }

    @Test
    @DisplayName("feriado cadastrado entra na contagem de dias uteis")
    void feriadoCadastradoAlteraContagem() {
        // Intimacao quinta 01/10/2026; termo inicial 02/10 (sexta).
        // Sem feriado: 02, 05, 06, 07 e 08 -> vence 08/10.
        ContagemPrazoStrategy semFeriado = new ContagemDiasUteis(calendarioCom());
        assertEquals(LocalDate.of(2026, 10, 8),
                semFeriado.calcularVencimento(LocalDate.of(2026, 10, 1), 5));

        // Com 06/10 feriado: 02, 05, 07, 08 e 09 -> vence 09/10.
        ContagemPrazoStrategy comFeriado = new ContagemDiasUteis(calendarioCom(new Feriado(
                "Feriado municipal", new DataUnica(LocalDate.of(2026, 10, 6)),
                Abrangencia.nacional())));
        assertEquals(LocalDate.of(2026, 10, 9),
                comFeriado.calcularVencimento(LocalDate.of(2026, 10, 1), 5));
    }

    // --- Invariantes do agregado ---

    @Test
    @DisplayName("feriado exige descricao, recorrencia e abrangencia")
    void invariantesDoFeriado() {
        RegraRecorrencia regra = new DataUnica(LocalDate.of(2026, 10, 15));

        assertThrows(IllegalArgumentException.class,
                () -> new Feriado(" ", regra, Abrangencia.nacional()));
        assertThrows(IllegalArgumentException.class,
                () -> new Feriado("Natal", null, Abrangencia.nacional()));
        assertThrows(IllegalArgumentException.class,
                () -> new Feriado("Natal", regra, null));
        assertThrows(IllegalArgumentException.class, () -> new DataUnica(null));
        assertThrows(IllegalArgumentException.class, () -> Jurisdicao.de("PE", " "));
    }
}
