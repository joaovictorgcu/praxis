package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.out.PrazoRepositorio;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.prazo.Prazo;

import java.util.NoSuchElementException;

/** Caso de uso: registrar cumprimento de prazo (peca protocolada). */
@Service
public class CumprirPrazoService implements PrazosUseCases.CumprirPrazo {

    private final PrazoRepositorio prazos;
    private final Relogio relogio;

    public CumprirPrazoService(PrazoRepositorio prazos, Relogio relogio) {
        this.prazos = prazos;
        this.relogio = relogio;
    }

    @Override
    @Transactional
    public Prazo executar(Long prazoId) {
        Prazo prazo = prazos.porId(prazoId)
                .orElseThrow(() -> new NoSuchElementException("prazo nao encontrado: " + prazoId));
        prazo.cumprir(relogio.hoje());
        return prazos.salvar(prazo);
    }
}
