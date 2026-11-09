package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.out.HistoricoDePrazoRepositorio;
import school.cesar.praxis.application.port.out.PrazoRepositorio;
import school.cesar.praxis.application.port.out.ProcessoRepositorio;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.prazo.ContagemPrazoStrategy;
import school.cesar.praxis.domain.prazo.EventoPrazo;
import school.cesar.praxis.domain.prazo.MotorDePrazos;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.prazo.TipoEventoPrazo;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.domain.processo.Processo;

import java.util.NoSuchElementException;

/**
 * Caso de uso: lancar prazo a partir de uma intimacao. Quem calcula o vencimento
 * e o dominio (Strategy escolhida pelo motor); aqui so ha orquestracao. A
 * abertura e o primeiro evento da trilha do prazo, na mesma transacao.
 */
@Service
public class AbrirPrazoService implements PrazosUseCases.AbrirPrazo {

    private final ProcessoRepositorio processos;
    private final PrazoRepositorio prazos;
    private final HistoricoDePrazoRepositorio historico;
    private final MotorDePrazos motor;
    private final Relogio relogio;

    public AbrirPrazoService(ProcessoRepositorio processos,
                             PrazoRepositorio prazos,
                             HistoricoDePrazoRepositorio historico,
                             MotorDePrazos motor,
                             Relogio relogio) {
        this.processos = processos;
        this.prazos = prazos;
        this.historico = historico;
        this.motor = motor;
        this.relogio = relogio;
    }

    @Override
    @Transactional
    public Prazo executar(Comando comando) {
        NumeroCnj numero = NumeroCnj.de(comando.numeroProcesso());
        Processo processo = processos.porNumero(numero)
                .orElseThrow(() -> new NoSuchElementException(
                        "processo não encontrado: " + comando.numeroProcesso()));
        if (comando.regime() == null) {
            throw new IllegalArgumentException("informe o regime de contagem do prazo");
        }

        ContagemPrazoStrategy contagem = motor.estrategiaPara(comando.regime());
        Prazo prazo = new Prazo(
                numero,
                comando.descricao(),
                comando.intimacao(),
                comando.quantidadeDias(),
                comando.fatal(),
                processo.getResponsavel(),
                comando.prerrogativa(),
                contagem);
        Prazo salvo = prazos.salvar(prazo);

        PrazosUseCases.Autor autor = PrazosUseCases.Autor.ouSistema(comando.autor());
        historico.registrar(EventoPrazo.novo(salvo.getId(), TipoEventoPrazo.ABERTURA, relogio.agora(),
                null, salvo.getVencimento(), TextoDaTrilhaDePrazo.abertura(salvo), autor.oab(), autor.nome()));
        return salvo;
    }
}
