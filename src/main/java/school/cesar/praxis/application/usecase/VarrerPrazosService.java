package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.out.ExecucaoVarreduraRepositorio;
import school.cesar.praxis.application.port.out.HistoricoDePrazoRepositorio;
import school.cesar.praxis.application.port.out.PrazoRepositorio;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.prazo.AlertaPrazo;
import school.cesar.praxis.domain.prazo.EventoPrazo;
import school.cesar.praxis.domain.prazo.ExecucaoVarredura;
import school.cesar.praxis.domain.prazo.MotorDePrazos;
import school.cesar.praxis.domain.prazo.OrigemVarredura;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.prazo.TipoEventoPrazo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Caso de uso central do motor de prazos: varre a agenda, deixa o dominio decidir
 * quais prazos merecem alerta e persiste, numa transacao so:
 * <ol>
 *   <li>os marcos ja emitidos de cada prazo alertado (a proxima varredura do
 *       mesmo dia nao repete a notificacao);</li>
 *   <li>um evento {@code ALERTA} na trilha de cada prazo alertado;</li>
 *   <li>a execucao em {@code varredura_execucao}, mesmo quando nada foi
 *       alertado - e a prova de que o motor rodou.</li>
 * </ol>
 * Prazo suspenso, cumprido ou cancelado nem entra: o repositorio so devolve os
 * prazos em aberto.
 */
@Service
public class VarrerPrazosService implements PrazosUseCases.VarrerPrazos, PrazosUseCases.ListarVarreduras {

    /** Quem aparece como solicitante quando o agendador dispara a varredura. */
    static final String AGENDADOR = "agendador diário";

    private final PrazoRepositorio prazos;
    private final HistoricoDePrazoRepositorio historico;
    private final ExecucaoVarreduraRepositorio execucoes;
    private final MotorDePrazos motor;
    private final Relogio relogio;

    public VarrerPrazosService(PrazoRepositorio prazos,
                               HistoricoDePrazoRepositorio historico,
                               ExecucaoVarreduraRepositorio execucoes,
                               MotorDePrazos motor,
                               Relogio relogio) {
        this.prazos = prazos;
        this.historico = historico;
        this.execucoes = execucoes;
        this.motor = motor;
        this.relogio = relogio;
    }

    @Override
    @Transactional
    public Resultado executar(Comando comando) {
        if (comando.dataReferencia() == null || comando.origem() == null) {
            throw new IllegalArgumentException("a varredura exige data de referência e origem");
        }
        LocalDateTime inicio = relogio.agora();
        List<Prazo> emAberto = prazos.emAberto();
        List<AlertaPrazo> alertas = motor.avaliarTodos(emAberto, comando.dataReferencia());

        // O motor marcou os niveis alertados nos agregados; persistimos so os que
        // alertaram nesta varredura (os demais nao mudaram).
        Map<Long, Prazo> porId = new LinkedHashMap<>();
        emAberto.forEach(prazo -> porId.put(prazo.getId(), prazo));
        String autor = comando.solicitante() == null ? "varredura" : comando.solicitante();
        alertas.stream().map(AlertaPrazo::prazoId).distinct()
                .forEach(id -> prazos.salvar(porId.get(id)));
        for (AlertaPrazo alerta : alertas) {
            historico.registrar(EventoPrazo.novo(alerta.prazoId(), TipoEventoPrazo.ALERTA, relogio.agora(),
                    alerta.vencimento(), alerta.vencimento(), TextoDaTrilhaDePrazo.alerta(alerta),
                    null, "varredura de " + TextoDaTrilhaDePrazo.data(comando.dataReferencia()) + " (" + autor + ")"));
        }

        LocalDateTime fim = relogio.agora();
        ExecucaoVarredura execucao = execucoes.registrar(new ExecucaoVarredura(
                null,
                comando.origem(),
                comando.dataReferencia(),
                inicio,
                fim.isBefore(inicio) ? inicio : fim,
                emAberto.size(),
                alertas.size(),
                comando.solicitante()));
        return new Resultado(execucao, alertas);
    }

    /** Sobrescrito para ter transacao propria: o default da interface chamaria {@code this} sem proxy. */
    @Override
    @Transactional
    public List<AlertaPrazo> executar(LocalDate hoje) {
        return executar(new Comando(hoje, OrigemVarredura.MANUAL, null)).alertas();
    }

    /**
     * Caminho do job agendado. Anotado tambem, porque a chamada interna a
     * {@code executar} nao passa pelo proxy do Spring e ficaria sem transacao.
     */
    @Override
    @Transactional
    public List<AlertaPrazo> executarHoje() {
        return executar(new Comando(relogio.hoje(), OrigemVarredura.AUTOMATICA, AGENDADOR)).alertas();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecucaoVarredura> executar(int limite) {
        return execucoes.ultimas(Math.max(1, Math.min(limite, 100)));
    }
}
