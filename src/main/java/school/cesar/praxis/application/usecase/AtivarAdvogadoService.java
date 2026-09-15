package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.AdvogadosUseCases;
import school.cesar.praxis.application.port.out.AdvogadoRepositorio;
import school.cesar.praxis.domain.advogado.Advogado;

import java.util.NoSuchElementException;

@Service
public class AtivarAdvogadoService implements AdvogadosUseCases.AtivarAdvogado {

    private final AdvogadoRepositorio advogados;

    public AtivarAdvogadoService(AdvogadoRepositorio advogados) {
        this.advogados = advogados;
    }

    @Override
    @Transactional
    public AdvogadosUseCases.ItemAdvogado executar(Long id) {
        Advogado advogado = advogados.porId(id)
                .orElseThrow(() -> new NoSuchElementException("advogado nao encontrado: " + id));
        advogado.ativar();
        return AdvogadoAppService.paraItem(advogados.salvar(advogado));
    }
}
