package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.FeriadosUseCases;
import school.cesar.praxis.application.port.out.FeriadoRepositorio;
import school.cesar.praxis.domain.feriado.Abrangencia;
import school.cesar.praxis.domain.feriado.DataUnica;
import school.cesar.praxis.domain.feriado.Feriado;
import school.cesar.praxis.domain.feriado.RecorrenciaAnualFixa;
import school.cesar.praxis.domain.feriado.RegraRecorrencia;
import school.cesar.praxis.domain.prazo.CalendarioForense;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Casos de uso do cadastro de feriados. Quem decide se a data incide e onde
 * vale e o dominio; aqui so ha traducao do comando e orquestracao.
 */
@Service
public class FeriadoAppService implements FeriadosUseCases.CadastrarFeriado,
        FeriadosUseCases.ListarFeriados,
        FeriadosUseCases.RemoverFeriado,
        FeriadosUseCases.ConsultarDiaUtil {

    private final FeriadoRepositorio feriados;
    private final CalendarioForense calendario;

    public FeriadoAppService(FeriadoRepositorio feriados, CalendarioForense calendario) {
        this.feriados = feriados;
        this.calendario = calendario;
    }

    @Override
    @Transactional
    public FeriadosUseCases.ItemFeriado executar(Comando comando) {
        if (comando.data() == null) {
            throw new IllegalArgumentException("data do feriado e obrigatoria");
        }

        // A escolha da Strategy e a unica decisao desta camada.
        RegraRecorrencia recorrencia = comando.repeteTodoAno()
                ? RecorrenciaAnualFixa.de(comando.data())
                : new DataUnica(comando.data());

        Feriado salvo = feriados.salvar(new Feriado(
                comando.descricao(),
                recorrencia,
                new Abrangencia(comando.nivel(), comando.abrangencia())));

        return paraItem(salvo);
    }

    @Override
    public List<FeriadosUseCases.ItemFeriado> executar() {
        return feriados.vigentes().stream()
                .sorted(Comparator.comparing(feriado ->
                        feriado.getRecorrencia().dataDeReferencia()))
                .map(FeriadoAppService::paraItem)
                .toList();
    }

    @Override
    @Transactional
    public void executar(Long id) {
        if (feriados.porId(id).isEmpty()) {
            throw new NoSuchElementException("feriado nao encontrado: " + id);
        }
        feriados.remover(id);
    }

    @Override
    public Resposta executar(LocalDate data) {
        return new Resposta(
                data,
                calendario.isDiaUtil(data),
                calendario.proximoDiaUtil(data));
    }

    private static FeriadosUseCases.ItemFeriado paraItem(Feriado feriado) {
        RegraRecorrencia recorrencia = feriado.getRecorrencia();
        return new FeriadosUseCases.ItemFeriado(
                feriado.getId(),
                feriado.getDescricao(),
                recorrencia.dataDeReferencia(),
                recorrencia instanceof RecorrenciaAnualFixa,
                recorrencia.rotulo(),
                feriado.getAbrangencia().rotulo());
    }
}
