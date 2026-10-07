package school.cesar.praxis.application.usecase;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.ComarcasUseCases;
import school.cesar.praxis.application.port.out.ComarcaRepositorio;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.jurisdicao.Comarca;

@Service
public class ComarcaAppService
    implements
        ComarcasUseCases.Cadastrar,
        ComarcasUseCases.Atualizar,
        ComarcasUseCases.Listar,
        ComarcasUseCases.BuscarPorId
{

    private final ComarcaRepositorio repositorio;
    private final Relogio relogio;

    public ComarcaAppService(ComarcaRepositorio repositorio, Relogio relogio) {
        this.repositorio = repositorio;
        this.relogio = relogio;
    }

    @Override
    @Transactional
    public ComarcasUseCases.ItemComarca executar(ComarcasUseCases.Cadastrar.Comando comando) {
        Comarca cadastro = new Comarca(
            null,
            comando.nome(),
            comando.municipio(),
            comando.uf(),
            comando.tribunal(),
            comando.endereco(),
            comando.telefone(),
            comando.email(),
            comando.horarioAtendimento(),
            comando.observacoes(),
            relogio.hoje()
        );
        validarCadastro(cadastro);
        return paraItem(repositorio.salvar(cadastro));
    }

    @Override
    @Transactional
    public ComarcasUseCases.ItemComarca executar(ComarcasUseCases.Atualizar.Comando comando) {
        Comarca anterior = carregar(comando.id());
        Comarca cadastro = new Comarca(
            anterior.id(),
            comando.nome(),
            comando.municipio(),
            comando.uf(),
            comando.tribunal(),
            comando.endereco(),
            comando.telefone(),
            comando.email(),
            comando.horarioAtendimento(),
            comando.observacoes(),
            anterior.dataCadastro()
        );
        validarCadastro(cadastro);
        return paraItem(repositorio.salvar(cadastro));
    }

    @Override
    public List<ComarcasUseCases.ItemComarca> executar() {
        return repositorio
            .listarTodas()
            .stream()
            .sorted(Comparator.comparing(Comarca::nome, String.CASE_INSENSITIVE_ORDER))
            .map(ComarcaAppService::paraItem)
            .toList();
    }

    @Override
    public ComarcasUseCases.ItemComarca executar(Long id) {
        return paraItem(carregar(id));
    }

    private Comarca carregar(Long id) {
        return repositorio.porId(id).orElseThrow(() -> new NoSuchElementException("Comarca não encontrada."));
    }

    private void validarCadastro(Comarca cadastro) {
        boolean duplicado = repositorio
            .listarTodas()
            .stream()
            .anyMatch(existente -> mesmoCadastro(existente, cadastro));
        if (duplicado) {
            throw new IllegalArgumentException(
                "Já existe uma comarca com este nome no município e estado informados."
            );
        }
    }

    private static boolean mesmoCadastro(Comarca existente, Comarca cadastro) {
        return (
            !Objects.equals(existente.id(), cadastro.id()) &&
            existente.nome().equalsIgnoreCase(cadastro.nome()) &&
            existente.municipio().equalsIgnoreCase(cadastro.municipio()) &&
            existente.uf().equals(cadastro.uf())
        );
    }

    private static ComarcasUseCases.ItemComarca paraItem(Comarca cadastro) {
        return new ComarcasUseCases.ItemComarca(
            cadastro.id(),
            cadastro.nome(),
            cadastro.municipio(),
            cadastro.uf(),
            cadastro.tribunal(),
            cadastro.endereco(),
            cadastro.telefone(),
            cadastro.email(),
            cadastro.horarioAtendimento(),
            cadastro.observacoes(),
            cadastro.dataCadastro()
        );
    }
}
