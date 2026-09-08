package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.out.PrazoRepositorio;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.prazo.MotorDePrazos;
import school.cesar.praxis.domain.prazo.Prazo;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/** Caso de uso de leitura: agenda de prazos ordenada por vencimento. */
@Service
public class ConsultarAgendaService implements PrazosUseCases.ConsultarAgenda {

    private final PrazoRepositorio prazos;
    private final MotorDePrazos motor;
    private final Relogio relogio;

    public ConsultarAgendaService(PrazoRepositorio prazos, MotorDePrazos motor, Relogio relogio) {
        this.prazos = prazos;
        this.motor = motor;
        this.relogio = relogio;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemAgenda> executar(LocalDate ate) {
        LocalDate hoje = relogio.hoje();
        return prazos.agendaAte(ate).stream()
                .sorted(Comparator.comparing(Prazo::getVencimento))
                .map(prazo -> new ItemAgenda(
                        prazo.getId(),
                        prazo.getNumeroProcesso().valor(),
                        prazo.getDescricao(),
                        prazo.getVencimento(),
                        prazo.diasRestantes(hoje, motor.estrategiaPara(prazo.getRegime())),
                        prazo.isFatal(),
                        prazo.venceu(hoje),
                        prazo.getResponsavel().nome()))
                .toList();
    }
}
