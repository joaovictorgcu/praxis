package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.out.HistoricoDePrazoRepositorio;
import school.cesar.praxis.application.port.out.PrazoRepositorio;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.prazo.EventoPrazo;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.prazo.TipoEventoPrazo;

import java.util.NoSuchElementException;

/** Caso de uso: registrar cumprimento de prazo (peca protocolada), com o evento na trilha. */
@Service
public class CumprirPrazoService implements PrazosUseCases.CumprirPrazo {

    private final PrazoRepositorio prazos;
    private final HistoricoDePrazoRepositorio historico;
    private final Relogio relogio;

    public CumprirPrazoService(PrazoRepositorio prazos, HistoricoDePrazoRepositorio historico, Relogio relogio) {
        this.prazos = prazos;
        this.historico = historico;
        this.relogio = relogio;
    }

    @Override
    @Transactional
    public Prazo executar(Long prazoId, PrazosUseCases.Autor autor) {
        Prazo prazo = prazos.porId(prazoId)
                .orElseThrow(() -> new NoSuchElementException("prazo não encontrado: " + prazoId));
        prazo.cumprir(relogio.hoje());
        Prazo salvo = prazos.salvar(prazo);

        PrazosUseCases.Autor quem = PrazosUseCases.Autor.ouSistema(autor);
        historico.registrar(EventoPrazo.novo(salvo.getId(), TipoEventoPrazo.CUMPRIMENTO, relogio.agora(),
                salvo.getVencimento(), salvo.getVencimento(), TextoDaTrilhaDePrazo.cumprimento(salvo),
                quem.oab(), quem.nome()));
        return salvo;
    }
}
