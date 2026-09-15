package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.AdvogadosUseCases;
import school.cesar.praxis.application.port.out.AdvogadoRepositorio;
import school.cesar.praxis.domain.advogado.Advogado;
import school.cesar.praxis.domain.compartilhado.Relogio;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AdvogadoAppService implements AdvogadosUseCases.CadastrarAdvogado,
        AdvogadosUseCases.ListarAdvogados,
        AdvogadosUseCases.PesquisarAdvogados,
        AdvogadosUseCases.AtualizarAdvogado {

    private final AdvogadoRepositorio advogados;
    private final Relogio relogio;

    public AdvogadoAppService(AdvogadoRepositorio advogados, Relogio relogio) {
        this.advogados = advogados;
        this.relogio = relogio;
    }

    @Override
    @Transactional
    public AdvogadosUseCases.ItemAdvogado executar(AdvogadosUseCases.CadastrarAdvogado.Comando comando) {
        if (advogados.porOab(comando.oab()).isPresent()) {
            throw new IllegalArgumentException("ja existe advogado com a OAB " + comando.oab());
        }

        Advogado salvo = advogados.salvar(new Advogado(
                comando.nome(),
                comando.email(),
                comando.oab(),
                comando.telefone(),
                comando.especialidade(),
                comando.disponivel(),
                relogio.hoje()));

        return paraItem(salvo);
    }

    @Override
    public List<AdvogadosUseCases.ItemAdvogado> executar() {
        return advogados.listarTodos().stream()
                .sorted(Comparator.comparing(Advogado::getNome))
                .map(AdvogadoAppService::paraItem)
                .toList();
    }

    @Override
    public List<AdvogadosUseCases.ItemAdvogado> executar(String termo) {
        return advogados.buscar(termo).stream()
                .sorted(Comparator.comparing(Advogado::getNome))
                .map(AdvogadoAppService::paraItem)
                .toList();
    }

    @Override
    @Transactional
    public AdvogadosUseCases.ItemAdvogado executar(AdvogadosUseCases.AtualizarAdvogado.Comando comando) {
        Advogado advogado = carregar(comando.id());
        advogado.atualizarDados(comando.nome(), comando.email(), comando.telefone(),
                comando.especialidade(), comando.disponivel());
        return paraItem(advogados.salvar(advogado));
    }

    private Advogado carregar(Long id) {
        return advogados.porId(id)
                .orElseThrow(() -> new NoSuchElementException("advogado nao encontrado: " + id));
    }

    static AdvogadosUseCases.ItemAdvogado paraItem(Advogado advogado) {
        return new AdvogadosUseCases.ItemAdvogado(
                advogado.getId(),
                advogado.getNome(),
                advogado.getEmail(),
                advogado.getOab(),
                advogado.getTelefone(),
                advogado.getEspecialidade(),
                advogado.getStatus(),
                advogado.isDisponivel(),
                advogado.getDataAdmissao());
    }
}
