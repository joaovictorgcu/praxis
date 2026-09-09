package school.cesar.praxis.domain.prazo;

import school.cesar.praxis.domain.feriado.FonteDeFeriados;
import school.cesar.praxis.domain.feriado.Jurisdicao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Servico de dominio: sabe quais dias nao contam para prazo processual.
 *
 * <p>Nao guarda mais a lista de feriados. Ele combina um conjunto de
 * {@link RegraDiaNaoUtil} (Composite), o que permite trocar a origem dos
 * feriados sem alterar esta classe nem as estrategias de contagem.
 */
public class CalendarioForense {

    private final List<RegraDiaNaoUtil> regras;

    public CalendarioForense(List<RegraDiaNaoUtil> regras) {
        if (regras == null || regras.isEmpty()) {
            throw new IllegalArgumentException("calendario exige ao menos uma regra");
        }
        this.regras = List.copyOf(regras);
    }

    /**
     * Calendario de datas fixas: fim de semana, recesso forense e o conjunto
     * informado. Usado no teste de unidade, que precisa ser previsivel.
     */
    public CalendarioForense(Set<LocalDate> feriados) {
        this(regrasPadrao(new FeriadosFixos(feriados)));
    }

    /**
     * Calendario do foro: os feriados vem do cadastro, ja filtrados pela
     * abrangencia (nacional, estadual ou da comarca).
     */
    public static CalendarioForense doForo(FonteDeFeriados fonte, Jurisdicao foro) {
        return new CalendarioForense(regrasPadrao(new FeriadosDoForo(fonte, foro)));
    }

    /** Fim de semana e recesso valem sempre; a origem dos feriados e que varia. */
    private static List<RegraDiaNaoUtil> regrasPadrao(RegraDiaNaoUtil feriados) {
        List<RegraDiaNaoUtil> padrao = new ArrayList<>();
        padrao.add(new FimDeSemana());
        padrao.add(new RecessoForense());
        padrao.add(feriados);
        return padrao;
    }

    public boolean isDiaUtil(LocalDate data) {
        for (RegraDiaNaoUtil regra : regras) {
            if (regra.suspende(data)) {
                return false;
            }
        }
        return true;
    }

    public LocalDate proximoDiaUtil(LocalDate data) {
        LocalDate cursor = data;
        while (!isDiaUtil(cursor)) {
            cursor = cursor.plusDays(1);
        }
        return cursor;
    }
}
