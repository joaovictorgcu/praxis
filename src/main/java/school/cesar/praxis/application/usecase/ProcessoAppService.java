package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.application.port.out.ProcessoRepositorio;
import school.cesar.praxis.domain.notificacao.ObservadorProcesso;
import school.cesar.praxis.domain.processo.Advogado;
import school.cesar.praxis.domain.processo.Andamento;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.domain.processo.Processo;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Casos de uso do subdominio nuclear. O observador (Observer) e reanexado ao
 * agregado a cada carregamento, porque a assinatura vive em memoria e o
 * agregado vem do banco.
 */
@Service
public class ProcessoAppService implements ProcessosUseCases.CadastrarProcesso,
        ProcessosUseCases.RegistrarAndamento,
        ProcessosUseCases.ConsultarLinhaDoTempo {

    private final ProcessoRepositorio processos;
    private final ObservadorProcesso observador;

    public ProcessoAppService(ProcessoRepositorio processos, ObservadorProcesso observador) {
        this.processos = processos;
        this.observador = observador;
    }

    @Override
    @Transactional
    public Processo executar(ProcessosUseCases.CadastrarProcesso.Comando comando) {
        Processo processo = new Processo(
                NumeroCnj.de(comando.numeroCnj()),
                comando.cliente(),
                comando.comarca(),
                comando.segredoJustica(),
                new Advogado(comando.responsavelNome(), comando.responsavelEmail(), comando.responsavelOab()));
        return processos.salvar(processo);
    }

    @Override
    @Transactional
    public Processo executar(ProcessosUseCases.RegistrarAndamento.Comando comando) {
        Processo processo = carregar(comando.numeroCnj());
        processo.assinar(observador);
        processo.registrarAndamento(new Andamento(comando.data(), comando.descricao(), comando.tipo()));
        return processos.salvar(processo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Andamento> executar(String numeroCnj) {
        Processo processo = carregar(numeroCnj);
        List<Andamento> timeline = new ArrayList<>();
        for (Andamento andamento : processo) {
            timeline.add(andamento);
        }
        return timeline;
    }

    private Processo carregar(String numeroCnj) {
        return processos.porNumero(NumeroCnj.de(numeroCnj))
                .orElseThrow(() -> new NoSuchElementException("processo nao encontrado: " + numeroCnj));
    }
}
