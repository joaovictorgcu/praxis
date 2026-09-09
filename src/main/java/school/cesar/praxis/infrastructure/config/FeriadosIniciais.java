package school.cesar.praxis.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import school.cesar.praxis.application.port.out.FeriadoRepositorio;
import school.cesar.praxis.domain.feriado.Abrangencia;
import school.cesar.praxis.domain.feriado.DataUnica;
import school.cesar.praxis.domain.feriado.Feriado;
import school.cesar.praxis.domain.feriado.RecorrenciaAnualFixa;

import java.time.LocalDate;
import java.time.MonthDay;

/**
 * Carga de referencia do calendario forense.
 *
 * <p>Sem feriado nenhum o motor de prazos calcularia vencimento errado, entao
 * isto nao e dado de demonstracao: roda tambem em teste. Por isso escuta
 * {@link ContextRefreshedEvent}, que dispara no {@code @SpringBootTest} -
 * diferente de {@code CommandLineRunner}, que so roda pela aplicacao.
 */
@Component
@ConditionalOnProperty(name = "praxis.feriados-iniciais", havingValue = "true",
        matchIfMissing = true)
public class FeriadosIniciais {

    private final FeriadoRepositorio feriados;

    public FeriadosIniciais(FeriadoRepositorio feriados) {
        this.feriados = feriados;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void carregar() {
        if (!feriados.vigentes().isEmpty()) {
            return;
        }

        // Feriados nacionais de data fixa: repetem todo ano (Strategy anual).
        anualNacional("Confraternizacao Universal", 1, 1);
        anualNacional("Tiradentes", 4, 21);
        anualNacional("Dia do Trabalho", 5, 1);
        anualNacional("Independencia do Brasil", 9, 7);
        anualNacional("Nossa Senhora Aparecida", 10, 12);
        anualNacional("Finados", 11, 2);
        anualNacional("Proclamacao da Republica", 11, 15);
        anualNacional("Consciencia Negra", 11, 20);
        anualNacional("Natal", 12, 25);

        // Feriados moveis: dependem da Pascoa, entao valem so em 2026 (data unica).
        dataUnicaNacional("Carnaval", LocalDate.of(2026, 2, 16));
        dataUnicaNacional("Carnaval", LocalDate.of(2026, 2, 17));
        dataUnicaNacional("Sexta-feira Santa", LocalDate.of(2026, 4, 3));
        dataUnicaNacional("Corpus Christi", LocalDate.of(2026, 6, 4));

        // Exemplos de abrangencia restrita, para o cadastro nascer demonstravel.
        feriados.salvar(new Feriado("Revolucao Pernambucana",
                new RecorrenciaAnualFixa(MonthDay.of(3, 6)),
                Abrangencia.estadual("PE")));
        feriados.salvar(new Feriado("Nossa Senhora da Conceicao",
                new RecorrenciaAnualFixa(MonthDay.of(12, 8)),
                Abrangencia.comarcal("Recife")));
    }

    private void anualNacional(String descricao, int mes, int dia) {
        feriados.salvar(new Feriado(descricao,
                new RecorrenciaAnualFixa(MonthDay.of(mes, dia)),
                Abrangencia.nacional()));
    }

    private void dataUnicaNacional(String descricao, LocalDate data) {
        feriados.salvar(new Feriado(descricao, new DataUnica(data), Abrangencia.nacional()));
    }
}
