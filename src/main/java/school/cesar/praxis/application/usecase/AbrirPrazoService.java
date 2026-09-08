package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.out.PrazoRepositorio;
import school.cesar.praxis.application.port.out.ProcessoRepositorio;
import school.cesar.praxis.domain.prazo.ContagemPrazoStrategy;
import school.cesar.praxis.domain.prazo.MotorDePrazos;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.processo.NumeroCnj;
import school.cesar.praxis.domain.processo.Processo;

import java.util.NoSuchElementException;

/**
 * Caso de uso: lancar prazo a partir de uma intimacao. Quem calcula o vencimento
 * e o dominio (Strategy escolhida pelo motor); aqui so ha orquestracao.
 */
@Service
public class AbrirPrazoService implements PrazosUseCases.AbrirPrazo {

    private final ProcessoRepositorio processos;
    private final PrazoRepositorio prazos;
    private final MotorDePrazos motor;

    public AbrirPrazoService(ProcessoRepositorio processos,
                             PrazoRepositorio prazos,
                             MotorDePrazos motor) {
        this.processos = processos;
        this.prazos = prazos;
        this.motor = motor;
    }

    @Override
    @Transactional
    public Prazo executar(Comando comando) {
        NumeroCnj numero = NumeroCnj.de(comando.numeroProcesso());
        Processo processo = processos.porNumero(numero)
                .orElseThrow(() -> new NoSuchElementException(
                        "processo nao encontrado: " + comando.numeroProcesso()));

        ContagemPrazoStrategy contagem = motor.estrategiaPara(comando.regime());

        Prazo prazo = new Prazo(
                numero,
                comando.descricao(),
                comando.intimacao(),
                comando.quantidadeDias(),
                comando.fatal(),
                processo.getResponsavel(),
                contagem);

        return prazos.salvar(prazo);
    }
}
