package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.out.PrazoRepositorio;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.prazo.AlertaPrazo;
import school.cesar.praxis.domain.prazo.MotorDePrazos;
import school.cesar.praxis.domain.prazo.Prazo;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Caso de uso central do motor de prazos: varre a agenda, deixa o dominio decidir
 * quais prazos merecem alerta e persiste os marcos ja emitidos, para que a
 * proxima varredura do mesmo dia nao repita a notificacao.
 */
@Service
public class VarrerPrazosService implements PrazosUseCases.VarrerPrazos {

    private final PrazoRepositorio prazos;
    private final MotorDePrazos motor;
    private final Relogio relogio;

    public VarrerPrazosService(PrazoRepositorio prazos, MotorDePrazos motor, Relogio relogio) {
        this.prazos = prazos;
        this.motor = motor;
        this.relogio = relogio;
    }

    @Override
    @Transactional
    public List<AlertaPrazo> executar(LocalDate hoje) {
        List<Prazo> emAberto = prazos.emAberto();
        List<AlertaPrazo> alertas = motor.avaliarTodos(emAberto, hoje);

        // O motor marcou os niveis alertados nos agregados; persistimos so os que
        // alertaram nesta varredura (os demais nao mudaram).
        Set<Long> alertados = new HashSet<>();
        for (AlertaPrazo alerta : alertas) {
            alertados.add(alerta.prazoId());
        }
        for (Prazo prazo : emAberto) {
            if (alertados.contains(prazo.getId())) {
                prazos.salvar(prazo);
            }
        }
        return alertas;
    }

    /**
     * Caminho do job agendado. Anotado tambem, porque a chamada interna a
     * {@code executar} nao passa pelo proxy do Spring e ficaria sem transacao.
     */
    @Override
    @Transactional
    public List<AlertaPrazo> executarHoje() {
        return executar(relogio.hoje());
    }
}
