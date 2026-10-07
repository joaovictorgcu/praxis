package school.cesar.praxis.application.usecase;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.VarasUseCases;
import school.cesar.praxis.application.port.out.ComarcaRepositorio;
import school.cesar.praxis.application.port.out.VaraRepositorio;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.jurisdicao.VaraJudicial;

@Service
public class VaraAppService
    implements
        VarasUseCases.Cadastrar,
        VarasUseCases.Atualizar,
        VarasUseCases.Listar,
        VarasUseCases.BuscarPorId
{

    private final VaraRepositorio repositorio;
    private final Relogio relogio;
    private final ComarcaRepositorio comarcas;

    public VaraAppService(VaraRepositorio repositorio, Relogio relogio, ComarcaRepositorio comarcas) {
        this.repositorio = repositorio;
        this.relogio = relogio;
        this.comarcas = comarcas;
    }

    @Override
    @Transactional
    public VarasUseCases.ItemVara executar(VarasUseCases.Cadastrar.Comando comando) {
        VaraJudicial cadastro = new VaraJudicial(
            null,
            comando.comarcaId(),
            comando.nome(),
            comando.competencia(),
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
    public VarasUseCases.ItemVara executar(VarasUseCases.Atualizar.Comando comando) {
        VaraJudicial anterior = carregar(comando.id());
        VaraJudicial cadastro = new VaraJudicial(
            anterior.id(),
            comando.comarcaId(),
            comando.nome(),
            comando.competencia(),
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
    public List<VarasUseCases.ItemVara> executar() {
        return repositorio
            .listarTodas()
            .stream()
            .sorted(Comparator.comparing(VaraJudicial::nome, String.CASE_INSENSITIVE_ORDER))
            .map(VaraAppService::paraItem)
            .toList();
    }

    @Override
    public VarasUseCases.ItemVara executar(Long id) {
        return paraItem(carregar(id));
    }

    private VaraJudicial carregar(Long id) {
        return repositorio.porId(id).orElseThrow(() -> new NoSuchElementException("Vara não encontrada."));
    }

    private void validarCadastro(VaraJudicial cadastro) {
        comarcas
            .porId(cadastro.comarcaId())
            .orElseThrow(() -> new IllegalArgumentException("A comarca selecionada não existe."));
        boolean duplicado = repositorio
            .listarTodas()
            .stream()
            .anyMatch(existente -> mesmoCadastro(existente, cadastro));
        if (duplicado) {
            throw new IllegalArgumentException("Já existe uma vara com este nome na comarca selecionada.");
        }
    }

    private static boolean mesmoCadastro(VaraJudicial existente, VaraJudicial cadastro) {
        return (
            !Objects.equals(existente.id(), cadastro.id()) &&
            existente.nome().equalsIgnoreCase(cadastro.nome()) &&
            existente.comarcaId().equals(cadastro.comarcaId())
        );
    }

    private static VarasUseCases.ItemVara paraItem(VaraJudicial cadastro) {
        return new VarasUseCases.ItemVara(
            cadastro.id(),
            cadastro.comarcaId(),
            cadastro.nome(),
            cadastro.competencia(),
            cadastro.endereco(),
            cadastro.telefone(),
            cadastro.email(),
            cadastro.horarioAtendimento(),
            cadastro.observacoes(),
            cadastro.dataCadastro()
        );
    }
}
