package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.AdvogadosUseCases;
import school.cesar.praxis.application.port.out.AdvogadoRepositorio;
import school.cesar.praxis.domain.advogado.Advogado;

import java.util.NoSuchElementException;

@Service
public class BuscarAdvogadoPorIdService implements AdvogadosUseCases.BuscarAdvogadoPorId {

    private final AdvogadoRepositorio advogados;

    public BuscarAdvogadoPorIdService(AdvogadoRepositorio advogados) {
        this.advogados = advogados;
    }

    @Override
    @Transactional(readOnly = true)
    public AdvogadosUseCases.ItemAdvogado executar(Long id) {
        Advogado advogado = advogados.porId(id)
                .orElseThrow(() -> new NoSuchElementException("advogado nao encontrado: " + id));
        return AdvogadoAppService.paraItem(advogado);
    }
}
