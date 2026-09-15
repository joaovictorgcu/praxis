package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import school.cesar.praxis.application.port.in.DistribuicaoUseCases;
import school.cesar.praxis.domain.distribuicao.CandidatoDistribuicao;
import school.cesar.praxis.domain.distribuicao.RegraDistribuicao;
import school.cesar.praxis.domain.distribuicao.RegraPadrao;
import school.cesar.praxis.domain.distribuicao.RegraPorDisponibilidade;
import school.cesar.praxis.domain.distribuicao.RegraPorEspecialidade;
import school.cesar.praxis.domain.notificacao.EventoProcesso;
import school.cesar.praxis.domain.notificacao.ObservadorProcesso;
import school.cesar.praxis.domain.processo.Advogado;

import java.util.List;

@Service
public class DistribuirProcessoService implements DistribuicaoUseCases.DistribuirProcesso {

    private final ObservadorProcesso observador;

    public DistribuirProcessoService(ObservadorProcesso observador) {
        this.observador = observador;
    }

    @Override
    public Advogado executar(Comando comando) {
        List<CandidatoDistribuicao> candidatos = comando.candidatos().stream()
                .map(c -> new CandidatoDistribuicao(
                        new Advogado(c.nome(), c.email(), c.oab()),
                        c.especialidade(),
                        c.processosAtivos(),
                        c.disponivel()))
                .toList();

        RegraDistribuicao especialidade = new RegraPorEspecialidade();
        RegraDistribuicao disponibilidade = new RegraPorDisponibilidade();
        RegraDistribuicao padrao = new RegraPadrao();
        especialidade.proximaRegra(disponibilidade);
        disponibilidade.proximaRegra(padrao);

        Advogado escolhido = especialidade.distribuir(comando.areaDireito(), candidatos);

        observador.notificar(new EventoProcesso.ProcessoDistribuido(
                comando.numeroProcesso(), escolhido, comando.areaDireito()));

        return escolhido;
    }
}