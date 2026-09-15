package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.AdvogadosUseCases;
import school.cesar.praxis.application.port.out.AdvogadoRepositorio;
import school.cesar.praxis.domain.advogado.Advogado;

import java.util.NoSuchElementException;

@Service
public class DesativarAdvogadoService implements AdvogadosUseCases.DesativarAdvogado {

    private final AdvogadoRepositorio advogados;

    public DesativarAdvogadoService(AdvogadoRepositorio advogados) {
        this.advogados = advogados;
    }

    @Override
    @Transactional
    public AdvogadosUseCases.ItemAdvogado executar(Long id) {
        Advogado advogado = advogados.porId(id)
                .orElseThrow(() -> new NoSuchElementException("advogado nao encontrado: " + id));
        advogado.desativar();
        return AdvogadoAppService.paraItem(advogados.salvar(advogado));
    }
}
