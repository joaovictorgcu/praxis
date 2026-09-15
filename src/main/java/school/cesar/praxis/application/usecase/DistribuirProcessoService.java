package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.DistribuicaoUseCases;
import school.cesar.praxis.application.port.out.AdvogadoRepositorio;
import school.cesar.praxis.application.port.out.EquipeRepositorio;
import school.cesar.praxis.application.port.out.ProcessoRepositorio;
import school.cesar.praxis.domain.advogado.Advogado;
import school.cesar.praxis.domain.advogado.StatusAdvogado;
import school.cesar.praxis.domain.distribuicao.CandidatoDistribuicao;
import school.cesar.praxis.domain.distribuicao.RegraDistribuicao;
import school.cesar.praxis.domain.distribuicao.RegraPadrao;
import school.cesar.praxis.domain.distribuicao.RegraPorDisponibilidade;
import school.cesar.praxis.domain.distribuicao.RegraPorEspecialidade;
import school.cesar.praxis.domain.equipe.Equipe;
import school.cesar.praxis.domain.notificacao.EventoProcesso;
import school.cesar.praxis.domain.notificacao.ObservadorProcesso;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DistribuirProcessoService implements DistribuicaoUseCases.DistribuirProcesso {

    private final AdvogadoRepositorio advogados;
    private final EquipeRepositorio equipes;
    private final ProcessoRepositorio processos;
    private final ObservadorProcesso observador;

    public DistribuirProcessoService(AdvogadoRepositorio advogados,
                                     EquipeRepositorio equipes,
                                     ProcessoRepositorio processos,
                                     ObservadorProcesso observador) {
        this.advogados = advogados;
        this.equipes = equipes;
        this.processos = processos;
        this.observador = observador;
    }

    @Override
    @Transactional
    public Advogado executar(Comando comando) {
        List<Advogado> base = comando.equipeId() != null
                ? advogadosDaEquipe(comando.equipeId())
                : advogados.listarTodos();

        List<CandidatoDistribuicao> candidatos = base.stream()
                .filter(advogado -> advogado.getStatus() == StatusAdvogado.ATIVO)
                .map(advogado -> new CandidatoDistribuicao(
                        advogado, processos.contarPorResponsavelOab(advogado.getOab())))
                .toList();

        RegraDistribuicao especialidade = new RegraPorEspecialidade();
        RegraDistribuicao disponibilidade = new RegraPorDisponibilidade();
        RegraDistribuicao padrao = new RegraPadrao();
        especialidade.proximaRegra(disponibilidade);
        disponibilidade.proximaRegra(padrao);

        Advogado escolhido = especialidade.distribuir(comando.areaDireito(), candidatos);

        observador.notificar(new EventoProcesso.ProcessoDistribuido(
                comando.numeroProcesso(),
                new school.cesar.praxis.domain.processo.Advogado(
                        escolhido.getNome(), escolhido.getEmail(), escolhido.getOab()),
                comando.areaDireito()));

        return escolhido;
    }

    private List<Advogado> advogadosDaEquipe(Long equipeId) {
        Equipe equipe = equipes.porId(equipeId)
                .orElseThrow(() -> new NoSuchElementException("equipe nao encontrada: " + equipeId));
        return advogados.listarPorIds(List.copyOf(equipe.getMembrosIds()));
    }
}
