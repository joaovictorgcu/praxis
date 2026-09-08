package school.cesar.praxis.infrastructure.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.domain.prazo.AlertaPrazo;

import java.util.List;

/**
 * Adaptador de entrada nao-HTTP: dispara a varredura da agenda todos os dias
 * as 7h. O caso de uso e o mesmo chamado pela API, entao o comportamento nao
 * depende de quem o aciona.
 */
@Component
public class VarreduraDePrazosJob {

    private static final Logger log = LoggerFactory.getLogger(VarreduraDePrazosJob.class);

    private final PrazosUseCases.VarrerPrazos varrerPrazos;

    public VarreduraDePrazosJob(PrazosUseCases.VarrerPrazos varrerPrazos) {
        this.varrerPrazos = varrerPrazos;
    }

    @Scheduled(cron = "${praxis.varredura-prazos.cron:0 0 7 * * MON-FRI}")
    public void executar() {
        List<AlertaPrazo> alertas = varrerPrazos.executarHoje();
        log.info("varredura de prazos concluida: {} alerta(s) emitido(s)", alertas.size());
    }
}
